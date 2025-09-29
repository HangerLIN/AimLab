package com.aimlab.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 比赛结果实体类
 */
@Data
public class CompetitionResult {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 比赛ID
     */
    private Integer competitionId;
    
    /**
     * 运动员ID
     */
    private Long athleteId;
    
    /**
     * 运动员姓名
     */
    private String athleteName;
    
    /**
     * 最终排名
     */
    private Integer finalRank;
    
    /**
     * 最终分数
     */
    private BigDecimal finalScore;
    
    /**
     * 总射击次数
     */
    private Integer totalShots;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 