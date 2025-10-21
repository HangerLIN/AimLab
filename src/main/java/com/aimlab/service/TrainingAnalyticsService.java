package com.aimlab.service;

import com.aimlab.dto.ExportFile;
import com.aimlab.dto.TrainingAthleteStatsDTO;
import com.aimlab.dto.TrainingProjectStatsDTO;
import com.aimlab.dto.TrainingTimeStatsDTO;
import com.aimlab.mapper.TrainingAnalyticsMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练统计分析服务
 */
@Service
public class TrainingAnalyticsService {

    private static final String DEFAULT_GRANULARITY = "DAY";

    @Autowired
    private TrainingAnalyticsMapper trainingAnalyticsMapper;

    public List<TrainingTimeStatsDTO> getTimeStats(String granularity,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime,
                                                   Long athleteId,
                                                   String projectType) {
        LocalDateTime[] range = normalizeRange(startTime, endTime);
        String normalizedGranularity = normalizeGranularity(granularity);
        String normalizedProject = normalizeText(projectType);
        return trainingAnalyticsMapper.aggregateByTime(normalizedGranularity, range[0], range[1], athleteId, normalizedProject);
    }

    public List<TrainingAthleteStatsDTO> getAthleteStats(LocalDateTime startTime,
                                                         LocalDateTime endTime,
                                                         String projectType,
                                                         String keyword) {
        LocalDateTime[] range = normalizeRange(startTime, endTime);
        String normalizedProject = normalizeText(projectType);
        String normalizedKeyword = normalizeText(keyword);
        return trainingAnalyticsMapper.aggregateByAthlete(range[0], range[1], normalizedProject, normalizedKeyword);
    }

    public List<TrainingProjectStatsDTO> getProjectStats(LocalDateTime startTime,
                                                         LocalDateTime endTime,
                                                         Long athleteId) {
        LocalDateTime[] range = normalizeRange(startTime, endTime);
        return trainingAnalyticsMapper.aggregateByProject(range[0], range[1], athleteId);
    }

    public ExportFile exportAnalytics(String dimension,
                                      String granularity,
                                      LocalDateTime startTime,
                                      LocalDateTime endTime,
                                      Long athleteId,
                                      String projectType,
                                      String keyword) {
        String normalizedDimension = dimension != null ? dimension.trim().toUpperCase() : "TIME";
        try (Workbook workbook = new XSSFWorkbook()) {
            switch (normalizedDimension) {
                case "ATHLETE":
                    List<TrainingAthleteStatsDTO> athleteStats = getAthleteStats(startTime, endTime, projectType, keyword);
                    writeAthleteSheet(workbook, athleteStats);
                    break;
                case "PROJECT":
                    List<TrainingProjectStatsDTO> projectStats = getProjectStats(startTime, endTime, athleteId);
                    writeProjectSheet(workbook, projectStats);
                    break;
                case "TIME":
                default:
                    List<TrainingTimeStatsDTO> timeStats = getTimeStats(granularity, startTime, endTime, athleteId, projectType);
                    writeTimeSheet(workbook, timeStats, normalizeGranularity(granularity));
                    break;
            }

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                workbook.write(bos);
                String fileName = "training-analytics-" + normalizedDimension.toLowerCase() + ".xlsx";
                return new ExportFile(fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());
            }
        } catch (Exception e) {
            throw new RuntimeException("导出训练统计失败: " + e.getMessage(), e);
        }
    }

    private void writeTimeSheet(Workbook workbook, List<TrainingTimeStatsDTO> stats, String granularity) {
        Sheet sheet = workbook.createSheet("TimeStats");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("周期(" + granularity + ")");
        header.createCell(1).setCellValue("平均环数");
        header.createCell(2).setCellValue("最高环数");
        header.createCell(3).setCellValue("最低环数");
        header.createCell(4).setCellValue("稳定性指数");
        header.createCell(5).setCellValue("射击总数");
        header.createCell(6).setCellValue("训练场次数");

        int rowIndex = 1;
        for (TrainingTimeStatsDTO item : stats) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getPeriod());
            row.createCell(1).setCellValue(toDouble(item.getAvgScore()));
            row.createCell(2).setCellValue(toDouble(item.getMaxScore()));
            row.createCell(3).setCellValue(toDouble(item.getMinScore()));
            row.createCell(4).setCellValue(toDouble(item.getStabilityIndex()));
            row.createCell(5).setCellValue(item.getTotalShots() == null ? 0 : item.getTotalShots());
            row.createCell(6).setCellValue(item.getSessionCount() == null ? 0 : item.getSessionCount());
        }
    }

    private void writeAthleteSheet(Workbook workbook, List<TrainingAthleteStatsDTO> stats) {
        Sheet sheet = workbook.createSheet("AthleteStats");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("运动员ID");
        header.createCell(1).setCellValue("运动员姓名");
        header.createCell(2).setCellValue("平均环数");
        header.createCell(3).setCellValue("最高环数");
        header.createCell(4).setCellValue("最低环数");
        header.createCell(5).setCellValue("稳定性指数");
        header.createCell(6).setCellValue("射击总数");
        header.createCell(7).setCellValue("训练场次数");

        int rowIndex = 1;
        for (TrainingAthleteStatsDTO item : stats) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getAthleteId() == null ? 0 : item.getAthleteId());
            row.createCell(1).setCellValue(item.getAthleteName() == null ? "" : item.getAthleteName());
            row.createCell(2).setCellValue(toDouble(item.getAvgScore()));
            row.createCell(3).setCellValue(toDouble(item.getMaxScore()));
            row.createCell(4).setCellValue(toDouble(item.getMinScore()));
            row.createCell(5).setCellValue(toDouble(item.getStabilityIndex()));
            row.createCell(6).setCellValue(item.getTotalShots() == null ? 0 : item.getTotalShots());
            row.createCell(7).setCellValue(item.getSessionCount() == null ? 0 : item.getSessionCount());
        }
    }

    private void writeProjectSheet(Workbook workbook, List<TrainingProjectStatsDTO> stats) {
        Sheet sheet = workbook.createSheet("ProjectStats");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("项目类型");
        header.createCell(1).setCellValue("平均环数");
        header.createCell(2).setCellValue("最高环数");
        header.createCell(3).setCellValue("最低环数");
        header.createCell(4).setCellValue("稳定性指数");
        header.createCell(5).setCellValue("射击总数");
        header.createCell(6).setCellValue("训练场次数");

        int rowIndex = 1;
        for (TrainingProjectStatsDTO item : stats) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getProjectType() == null ? "未设置项目" : item.getProjectType());
            row.createCell(1).setCellValue(toDouble(item.getAvgScore()));
            row.createCell(2).setCellValue(toDouble(item.getMaxScore()));
            row.createCell(3).setCellValue(toDouble(item.getMinScore()));
            row.createCell(4).setCellValue(toDouble(item.getStabilityIndex()));
            row.createCell(5).setCellValue(item.getTotalShots() == null ? 0 : item.getTotalShots());
            row.createCell(6).setCellValue(item.getSessionCount() == null ? 0 : item.getSessionCount());
        }
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private String normalizeGranularity(String granularity) {
        if (granularity == null || granularity.trim().isEmpty()) {
            return DEFAULT_GRANULARITY;
        }
        String upper = granularity.trim().toUpperCase();
        switch (upper) {
            case "WEEK":
            case "MONTH":
            case "DAY":
                return upper;
            default:
                return DEFAULT_GRANULARITY;
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDateTime[] normalizeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        LocalDateTime start = startTime != null ? startTime : end.minusDays(30);
        if (start.isAfter(end)) {
            LocalDateTime tmp = start;
            start = end;
            end = tmp;
        }
        return new LocalDateTime[]{start, end};
    }
}
