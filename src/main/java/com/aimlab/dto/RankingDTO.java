package com.aimlab.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 排行榜DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingDTO {
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 运动员ID
     */
    private Long athleteId;
    
    /**
     * 运动员姓名
     */
    private String athleteName;
    
    /**
     * 运动员等级
     */
    private String athleteLevel;
    
    /**
     * 性别
     */
    private String gender;
    
    /**
     * 平均分数
     */
    private BigDecimal averageScore;
    
    /**
     * 最高分数
     */
    private BigDecimal maxScore;
    
    /**
     * 比赛/训练次数
     */
    private Integer participationCount;
    
    /**
     * 总环数/总分数
     */
    private BigDecimal totalScore;
    
    /**
     * 胜率（仅用于全站排行）
     */
    private BigDecimal winRate;
}
