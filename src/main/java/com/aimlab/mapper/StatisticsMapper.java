package com.aimlab.mapper;

import com.aimlab.dto.StatisticsReportDTO;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

/**
 * 统计报表数据访问接口
 */
public interface StatisticsMapper {
    
    /**
     * 获取月度训练统计报表
     * @param year 年份
     * @param month 月份
     * @return 统计报表
     */
    StatisticsReportDTO getMonthlyTrainingStatistics(@Param("year") Integer year, @Param("month") Integer month);
    
    /**
     * 获取月度比赛成绩统计报表
     * @param year 年份
     * @param month 月份
     * @return 统计报表
     */
    StatisticsReportDTO getMonthlyCompetitionStatistics(@Param("year") Integer year, @Param("month") Integer month);
    
    /**
     * 获取季度统计报表
     * @param year 年份
     * @param quarter 季度
     * @param reportType 报表类型：TRAINING, COMPETITION, OVERALL
     * @return 统计报表
     */
    StatisticsReportDTO getQuarterlyStatistics(@Param("year") Integer year, @Param("quarter") Integer quarter, @Param("reportType") String reportType);
    
    /**
     * 获取年度统计报表
     * @param year 年份
     * @param reportType 报表类型：TRAINING, COMPETITION, OVERALL
     * @return 统计报表
     */
    StatisticsReportDTO getYearlyStatistics(@Param("year") Integer year, @Param("reportType") String reportType);
    
    /**
     * 获取自定义时间范围的统计报表
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param reportType 报表类型：TRAINING, COMPETITION, OVERALL
     * @return 统计报表
     */
    StatisticsReportDTO getCustomStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("reportType") String reportType);
    
    /**
     * 获取运动员个人月度统计
     * @param athleteId 运动员ID
     * @param year 年份
     * @param month 月份
     * @return 统计报表
     */
    StatisticsReportDTO getAthleteMonthlyStatistics(@Param("athleteId") Long athleteId, @Param("year") Integer year, @Param("month") Integer month);
    
    /**
     * 获取运动员个人年度统计
     * @param athleteId 运动员ID
     * @param year 年份
     * @return 统计报表
     */
    StatisticsReportDTO getAthleteYearlyStatistics(@Param("athleteId") Long athleteId, @Param("year") Integer year);
}
