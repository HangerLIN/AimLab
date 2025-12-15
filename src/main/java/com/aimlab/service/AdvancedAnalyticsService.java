package com.aimlab.service;

import com.aimlab.dto.*;
import com.aimlab.mapper.AdvancedAnalyticsMapper;
import com.aimlab.mapper.AthleteMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdvancedAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AdvancedAnalyticsService.class);

    private static final int DEFAULT_WEEKS = 12;
    private static final int MOVING_AVERAGE_WINDOW = 3;
    private static final int PREDICTION_WEEKS = 2;

    @Autowired
    private AdvancedAnalyticsMapper analyticsMapper;

    @Autowired
    private AthleteMapper athleteMapper;

    /**
     * 运动员对比分析
     */
    public List<AthleteCompareDTO> compareAthletes(CompareRequestDTO request) {
        log.info("开始运动员对比分析, 请求参数: level={}, athleteIds={}, maxCount={}", 
                request.getLevel(), request.getAthleteIds(), request.getMaxCount());
        
        LocalDateTime endTime = request.getEndTime() != null ? 
                request.getEndTime() : LocalDateTime.now();
        LocalDateTime startTime = request.getStartTime() != null ? 
                request.getStartTime() : endTime.minusDays(30);
        
        int maxCount = request.getMaxCount() != null ? request.getMaxCount() : 5;
        
        List<AthleteCompareDTO> result;
        
        try {
            if (request.getAthleteIds() != null && !request.getAthleteIds().isEmpty()) {
                result = analyticsMapper.getAthletesByIds(
                        request.getAthleteIds(), startTime, endTime, request.getProjectType());
            } else {
                result = analyticsMapper.getAthleteCompareData(
                        request.getLevel(), null, startTime, endTime, 
                        request.getProjectType(), maxCount);
            }
            
            log.info("查询到 {} 条运动员对比数据", result.size());
            
            // 计算稳定性得分
            for (AthleteCompareDTO dto : result) {
                if (dto.getStabilityIndex() != null) {
                    BigDecimal score = BigDecimal.valueOf(100)
                            .subtract(dto.getStabilityIndex().multiply(BigDecimal.TEN));
                    dto.setStabilityScore(score.max(BigDecimal.ZERO));
                } else {
                    dto.setStabilityScore(BigDecimal.valueOf(100));
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("运动员对比分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("运动员对比分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 趋势分析
     */
    public TrendAnalysisResultDTO getTrendAnalysis(Long athleteId, Integer weeks, String projectType) {
        log.info("开始趋势分析, athleteId={}, weeks={}, projectType={}", athleteId, weeks, projectType);
        
        int analyzeWeeks = weeks != null && weeks > 0 ? weeks : DEFAULT_WEEKS;
        
        try {
            List<TrendDataDTO> rawData = analyticsMapper.getWeeklyTrendData(
                    athleteId, analyzeWeeks, projectType);
            
            log.info("查询到 {} 周的趋势数据", rawData.size());
            
            if (rawData.isEmpty()) {
                log.warn("运动员 {} 在最近 {} 周内没有足够的训练数据", athleteId, analyzeWeeks);
                TrendAnalysisResultDTO emptyResult = new TrendAnalysisResultDTO();
                emptyResult.setAthleteId(athleteId);
                emptyResult.setAnalyzedWeeks(0);
                emptyResult.setTrendData(Collections.emptyList());
                emptyResult.setTrendDirection("STABLE");
                return emptyResult;
            }
            
            // 计算移动平均
            calculateMovingAverage(rawData, MOVING_AVERAGE_WINDOW);
            rawData.forEach(d -> d.setIsPredicted(false));
            
            // 计算趋势斜率
            BigDecimal slope = calculateTrendSlope(rawData);
            String direction = determineTrendDirection(slope);
            
            log.debug("趋势斜率: {}, 方向: {}", slope, direction);
            
            // 生成预测
            List<TrendDataDTO> predictions = predictFutureWeeks(rawData, PREDICTION_WEEKS, slope);
            
            // 计算摘要
            TrendAnalysisResultDTO.TrendSummary summary = calculateSummary(rawData);
            
            // 获取运动员信息
            var athlete = athleteMapper.findById(athleteId);
            
            TrendAnalysisResultDTO result = new TrendAnalysisResultDTO();
            result.setAthleteId(athleteId);
            result.setAthleteName(athlete != null ? athlete.getName() : "未知运动员");
            result.setAnalyzedWeeks(rawData.size());
            result.setTrendData(rawData);
            result.setTrendDirection(direction);
            result.setTrendSlope(slope);
            result.setPredictions(predictions);
            result.setSummary(summary);
            
            log.info("趋势分析完成, 运动员: {}, 分析周数: {}, 趋势: {}", 
                    result.getAthleteName(), result.getAnalyzedWeeks(), direction);
            
            return result;
        } catch (Exception e) {
            log.error("趋势分析失败, athleteId={}: {}", athleteId, e.getMessage(), e);
            throw new RuntimeException("趋势分析失败: " + e.getMessage(), e);
        }
    }

    private void calculateMovingAverage(List<TrendDataDTO> data, int window) {
        for (int i = 0; i < data.size(); i++) {
            int start = Math.max(0, i - window + 1);
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;
            
            for (int j = start; j <= i; j++) {
                if (data.get(j).getAvgScore() != null) {
                    sum = sum.add(data.get(j).getAvgScore());
                    count++;
                }
            }
            
            if (count > 0) {
                data.get(i).setMovingAverage(
                        sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
            }
        }
    }

    private BigDecimal calculateTrendSlope(List<TrendDataDTO> data) {
        if (data.size() < 2) return BigDecimal.ZERO;
        
        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = data.get(i).getAvgScore() != null ? 
                    data.get(i).getAvgScore().doubleValue() : 0;
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) return BigDecimal.ZERO;
        
        double slope = (n * sumXY - sumX * sumY) / denominator;
        return BigDecimal.valueOf(slope).setScale(4, RoundingMode.HALF_UP);
    }

    private String determineTrendDirection(BigDecimal slope) {
        double slopeValue = slope.doubleValue();
        if (slopeValue > 0.05) return "UP";
        else if (slopeValue < -0.05) return "DOWN";
        else return "STABLE";
    }

    private List<TrendDataDTO> predictFutureWeeks(List<TrendDataDTO> data, 
                                                   int predictWeeks, 
                                                   BigDecimal slope) {
        if (data.isEmpty()) return Collections.emptyList();
        
        List<TrendDataDTO> predictions = new ArrayList<>();
        TrendDataDTO lastData = data.get(data.size() - 1);
        BigDecimal lastScore = lastData.getMovingAverage() != null ? 
                lastData.getMovingAverage() : lastData.getAvgScore();
        
        if (lastScore == null) return Collections.emptyList();
        
        for (int i = 1; i <= predictWeeks; i++) {
            TrendDataDTO prediction = new TrendDataDTO();
            prediction.setPeriod("预测第" + i + "周");
            prediction.setWeekNumber(lastData.getWeekNumber() + i);
            prediction.setIsPredicted(true);
            
            BigDecimal predictedScore = lastScore.add(slope.multiply(BigDecimal.valueOf(i)));
            predictedScore = predictedScore.max(BigDecimal.ZERO).min(BigDecimal.TEN);
            prediction.setPredictedScore(predictedScore.setScale(2, RoundingMode.HALF_UP));
            prediction.setAvgScore(prediction.getPredictedScore());
            
            predictions.add(prediction);
        }
        
        return predictions;
    }

    private TrendAnalysisResultDTO.TrendSummary calculateSummary(List<TrendDataDTO> data) {
        TrendAnalysisResultDTO.TrendSummary summary = new TrendAnalysisResultDTO.TrendSummary();
        
        if (data.isEmpty()) return summary;
        
        BigDecimal highest = BigDecimal.ZERO;
        BigDecimal lowest = BigDecimal.valueOf(10);
        BigDecimal sum = BigDecimal.ZERO;
        String bestWeek = "", worstWeek = "";
        
        for (TrendDataDTO d : data) {
            if (d.getAvgScore() != null) {
                if (d.getAvgScore().compareTo(highest) > 0) {
                    highest = d.getAvgScore();
                    bestWeek = d.getPeriod();
                }
                if (d.getAvgScore().compareTo(lowest) < 0) {
                    lowest = d.getAvgScore();
                    worstWeek = d.getPeriod();
                }
                sum = sum.add(d.getAvgScore());
            }
        }
        
        summary.setPeriodHighest(highest);
        summary.setPeriodLowest(lowest);
        summary.setPeriodAverage(sum.divide(BigDecimal.valueOf(data.size()), 2, RoundingMode.HALF_UP));
        summary.setBestWeek(bestWeek);
        summary.setWorstWeek(worstWeek);
        
        if (data.size() >= 2) {
            BigDecimal first = data.get(0).getAvgScore();
            BigDecimal last = data.get(data.size() - 1).getAvgScore();
            if (first != null && last != null) {
                summary.setImprovement(last.subtract(first).setScale(2, RoundingMode.HALF_UP));
            }
        }
        
        return summary;
    }

    /**
     * 导出对比数据
     */
    public ExportFile exportCompareData(List<AthleteCompareDTO> data, String format) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("运动员对比分析");
            
            Row header = sheet.createRow(0);
            String[] headers = {"运动员ID", "姓名", "级别", "平均环数", "最高环数", 
                    "最低环数", "稳定性指数", "射击总数", "训练场次", "10环率(%)", "9环以上率(%)"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            
            int rowIndex = 1;
            for (AthleteCompareDTO dto : data) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(dto.getAthleteId() != null ? dto.getAthleteId() : 0);
                row.createCell(1).setCellValue(dto.getAthleteName() != null ? dto.getAthleteName() : "");
                row.createCell(2).setCellValue(dto.getAthleteLevel() != null ? dto.getAthleteLevel() : "");
                row.createCell(3).setCellValue(toDouble(dto.getAvgScore()));
                row.createCell(4).setCellValue(toDouble(dto.getMaxScore()));
                row.createCell(5).setCellValue(toDouble(dto.getMinScore()));
                row.createCell(6).setCellValue(toDouble(dto.getStabilityIndex()));
                row.createCell(7).setCellValue(dto.getTotalShots() != null ? dto.getTotalShots() : 0);
                row.createCell(8).setCellValue(dto.getSessionCount() != null ? dto.getSessionCount() : 0);
                row.createCell(9).setCellValue(toDouble(dto.getPerfectRate()));
                row.createCell(10).setCellValue(toDouble(dto.getHighScoreRate()));
            }
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            
            String fileName = "athlete-compare-" + System.currentTimeMillis() + ".xlsx";
            return new ExportFile(fileName, 
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                    bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("导出对比数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出趋势数据
     */
    public ExportFile exportTrendData(TrendAnalysisResultDTO data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("趋势分析");
            
            Row header = sheet.createRow(0);
            String[] headers = {"周期", "平均环数", "最高环数", "最低环数", 
                    "稳定性指数", "移动平均", "射击总数", "训练场次", "是否预测"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            
            int rowIndex = 1;
            for (TrendDataDTO dto : data.getTrendData()) {
                fillTrendRow(sheet.createRow(rowIndex++), dto);
            }
            for (TrendDataDTO dto : data.getPredictions()) {
                fillTrendRow(sheet.createRow(rowIndex++), dto);
            }
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            
            String fileName = "trend-analysis-" + data.getAthleteId() + ".xlsx";
            return new ExportFile(fileName, 
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                    bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("导出趋势数据失败: " + e.getMessage(), e);
        }
    }

    private void fillTrendRow(Row row, TrendDataDTO dto) {
        row.createCell(0).setCellValue(dto.getPeriod() != null ? dto.getPeriod() : "");
        row.createCell(1).setCellValue(toDouble(dto.getAvgScore()));
        row.createCell(2).setCellValue(toDouble(dto.getMaxScore()));
        row.createCell(3).setCellValue(toDouble(dto.getMinScore()));
        row.createCell(4).setCellValue(toDouble(dto.getStabilityIndex()));
        row.createCell(5).setCellValue(toDouble(dto.getMovingAverage()));
        row.createCell(6).setCellValue(dto.getTotalShots() != null ? dto.getTotalShots() : 0);
        row.createCell(7).setCellValue(dto.getSessionCount() != null ? dto.getSessionCount() : 0);
        row.createCell(8).setCellValue(Boolean.TRUE.equals(dto.getIsPredicted()) ? "是" : "否");
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}
