package com.aimlab.controller;

import com.aimlab.dto.StatisticsReportDTO;
import com.aimlab.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * 统计报表接口控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * 获取月度训练统计报表
     * GET /api/statistics/training/monthly?year=2025&month=12
     */
    @GetMapping("/training/monthly")
    public StatisticsReportDTO getMonthlyTrainingStatistics(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        if (year == null || month == null) {
            return statisticsService.getCurrentMonthTrainingStatistics();
        }
        return statisticsService.getMonthlyTrainingStatistics(year, month);
    }
    
    /**
     * 获取月度比赛成绩统计报表
     * GET /api/statistics/competition/monthly?year=2025&month=12
     */
    @GetMapping("/competition/monthly")
    public StatisticsReportDTO getMonthlyCompetitionStatistics(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        if (year == null || month == null) {
            return statisticsService.getCurrentMonthCompetitionStatistics();
        }
        return statisticsService.getMonthlyCompetitionStatistics(year, month);
    }
    
    /**
     * 获取季度统计报表
     * GET /api/statistics/quarterly?year=2025&quarter=4&reportType=OVERALL
     */
    @GetMapping("/quarterly")
    public StatisticsReportDTO getQuarterlyStatistics(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "quarter", required = false) Integer quarter,
            @RequestParam(value = "reportType", defaultValue = "OVERALL") String reportType) {
        if (year == null || quarter == null) {
            return statisticsService.getCurrentQuarterStatistics(reportType);
        }
        return statisticsService.getQuarterlyStatistics(year, quarter, reportType);
    }
    
    /**
     * 获取年度统计报表
     * GET /api/statistics/yearly?year=2025&reportType=OVERALL
     */
    @GetMapping("/yearly")
    public StatisticsReportDTO getYearlyStatistics(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "reportType", defaultValue = "OVERALL") String reportType) {
        if (year == null) {
            return statisticsService.getCurrentYearStatistics(reportType);
        }
        return statisticsService.getYearlyStatistics(year, reportType);
    }
    
    /**
     * 获取自定义时间范围的统计报表
     * GET /api/statistics/custom?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59&reportType=OVERALL
     */
    @GetMapping("/custom")
    public StatisticsReportDTO getCustomStatistics(
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "reportType", defaultValue = "OVERALL") String reportType) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        return statisticsService.getCustomStatistics(start, end, reportType);
    }
    
    /**
     * 获取运动员个人月度统计
     * GET /api/statistics/athlete/1/monthly?year=2025&month=12
     */
    @GetMapping("/athlete/{athleteId}/monthly")
    public StatisticsReportDTO getAthleteMonthlyStatistics(
            @PathVariable Long athleteId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        if (year == null || month == null) {
            return statisticsService.getAthleteCurrentMonthStatistics(athleteId);
        }
        return statisticsService.getAthleteMonthlyStatistics(athleteId, year, month);
    }
    
    /**
     * 获取运动员个人年度统计
     * GET /api/statistics/athlete/1/yearly?year=2025
     */
    @GetMapping("/athlete/{athleteId}/yearly")
    public StatisticsReportDTO getAthleteYearlyStatistics(
            @PathVariable Long athleteId,
            @RequestParam(value = "year", required = false) Integer year) {
        if (year == null) {
            return statisticsService.getAthleteCurrentYearStatistics(athleteId);
        }
        return statisticsService.getAthleteYearlyStatistics(athleteId, year);
    }
}
