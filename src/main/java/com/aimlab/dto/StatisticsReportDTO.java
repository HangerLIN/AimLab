package com.aimlab.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * 统计报表DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsReportDTO {
    
    /**
     * 报表类型：TRAINING(训练), COMPETITION(比赛), OVERALL(综合)
     */
    private String reportType;
    
    /**
     * 统计周期：MONTHLY(月度), QUARTERLY(季度), YEARLY(年度), CUSTOM(自定义)
     */
    private String period;
    
    /**
     * 统计时间范围
     */
    private String timeRange;
    
    /**
     * 总参与人数
     */
    private Integer totalParticipants;
    
    /**
     * 总参与次数
     */
    private Integer totalParticipations;
    
    /**
     * 平均分数
     */
    private BigDecimal averageScore;
    
    /**
     * 最高分数
     */
    private BigDecimal maxScore;
    
    /**
     * 最低分数
     */
    private BigDecimal minScore;
    
    /**
     * 总环数/总分数
     */
    private BigDecimal totalScore;
    
    /**
     * 数据详情列表
     */
    private List<AthleteStatisticsDTO> details;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class AthleteStatisticsDTO {
    
    private Long athleteId;
    
    private String athleteName;
    
    private String athleteLevel;
    
    /**
     * 本周期参与次数
     */
    private Integer participationCount;
    
    /**
     * 本周期平均分数
     */
    private BigDecimal averageScore;
    
    /**
     * 本周期最高分数
     */
    private BigDecimal maxScore;
    
    /**
     * 本周期最低分数
     */
    private BigDecimal minScore;
    
    /**
     * 本周期总分数
     */
    private BigDecimal totalScore;
    
    /**
     * 本周期进度（与上周期的对比）
     */
    private BigDecimal progressRate;
}
