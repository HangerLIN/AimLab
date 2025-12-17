package com.aimlab.service;

import com.aimlab.dto.StatisticsReportDTO;
import com.aimlab.mapper.StatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoField;

/**
 * 统计报表服务类
 */
@Service
public class StatisticsService {
    
    @Autowired
    private StatisticsMapper statisticsMapper;
    
    /**
     * 获取月度训练统计报表
     * @param year 年份
     * @param month 月份
     * @return 统计报表
     */
    public StatisticsReportDTO getMonthlyTrainingStatistics(Integer year, Integer month) {
        return statisticsMapper.getMonthlyTrainingStatistics(year, month);
    }
    
    /**
     * 获取当前月份的训练统计报表
     * @return 统计报表
     */
    public StatisticsReportDTO getCurrentMonthTrainingStatistics() {
        YearMonth now = YearMonth.now();
        return getMonthlyTrainingStatistics(now.getYear(), now.getMonthValue());
    }
    
    /**
     * 获取月度比赛成绩统计报表
     * @param year 年份
     * @param month 月份
     * @return 统计报表
     */
    public StatisticsReportDTO getMonthlyCompetitionStatistics(Integer year, Integer month) {
        return statisticsMapper.getMonthlyCompetitionStatistics(year, month);
    }
    
    /**
     * 获取当前月份的比赛成绩统计报表
     * @return 统计报表
     */
    public StatisticsReportDTO getCurrentMonthCompetitionStatistics() {
        YearMonth now = YearMonth.now();
        return getMonthlyCompetitionStatistics(now.getYear(), now.getMonthValue());
    }
    
    /**
     * 获取季度统计报表
     * @param year 年份
     * @param quarter 季度（1-4）
     * @param reportType 报表类型：TRAINING, COMPETITION, OVERALL
     * @return 统计报表
     */
    public StatisticsReportDTO getQuarterlyStatistics(Integer year, Integer quarter, String reportType) {
        validateQuarter(quarter);
        if (reportType == null) {
            reportType = "OVERALL";
        }
        return statisticsMapper.getQuarterlyStatistics(year, quarter, reportType);
    }
    
    /**
     * 获取当前季度的统计报表
     * @param reportType 报表类型：TRAINING, COMPETITION, OVERALL
     * @return 统计报表
     */
    public StatisticsReportDTO getCurrentQuarterStatistics(String reportType) {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;
        return getQuarterlyStatistics(year, quarter, reportType);
    }
    
    /**
     * 获取年度统计报表
     * @param year 年份
     * @param reportType 报表类型：TRAINING, COMPETITION, OVERALL
     * @return 统计报表
     */
    public StatisticsReportDTO getYearlyStatistics(Integer year, String reportType) {
        if (reportType == null) {
            reportType = "OVERALL";
        }
        return statisticsMapper.getYearlyStatistics(year, reportType);
    }
    
    /**
     * 获取当前年度的统计报表
     * @param reportType 报表类型：TRAINING, COMPETITION, OVERALL
     * @return 统计报表
     */
    public StatisticsReportDTO getCurrentYearStatistics(String reportType) {
        int year = LocalDateTime.now().getYear();
        return getYearlyStatistics(year, reportType);
    }
    
    /**
     * 获取自定义时间范围的统计报表
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param reportType 报表类型：TRAINING, COMPETITION, OVERALL
     * @return 统计报表
     */
    public StatisticsReportDTO getCustomStatistics(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        if (reportType == null) {
            reportType = "OVERALL";
        }
        return statisticsMapper.getCustomStatistics(startDate, endDate, reportType);
    }
    
    /**
     * 获取运动员个人月度统计
     * @param athleteId 运动员ID
     * @param year 年份
     * @param month 月份
     * @return 统计报表
     */
    public StatisticsReportDTO getAthleteMonthlyStatistics(Long athleteId, Integer year, Integer month) {
        return statisticsMapper.getAthleteMonthlyStatistics(athleteId, year, month);
    }
    
    /**
     * 获取运动员当前月度统计
     * @param athleteId 运动员ID
     * @return 统计报表
     */
    public StatisticsReportDTO getAthleteCurrentMonthStatistics(Long athleteId) {
        YearMonth now = YearMonth.now();
        return getAthleteMonthlyStatistics(athleteId, now.getYear(), now.getMonthValue());
    }
    
    /**
     * 获取运动员个人年度统计
     * @param athleteId 运动员ID
     * @param year 年份
     * @return 统计报表
     */
    public StatisticsReportDTO getAthleteYearlyStatistics(Long athleteId, Integer year) {
        return statisticsMapper.getAthleteYearlyStatistics(athleteId, year);
    }
    
    /**
     * 获取运动员当前年度统计
     * @param athleteId 运动员ID
     * @return 统计报表
     */
    public StatisticsReportDTO getAthleteCurrentYearStatistics(Long athleteId) {
        int year = LocalDateTime.now().getYear();
        return getAthleteYearlyStatistics(athleteId, year);
    }
    
    /**
     * 验证季度是否有效
     */
    private void validateQuarter(Integer quarter) {
        if (quarter == null || quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("季度必须为1-4之间的数字");
        }
    }
}
