package com.aimlab.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 比赛排名项数据传输对象
 */
@Data
public class RankingItemDTO {
    
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
     * 总分数
     */
    private BigDecimal totalScore;
    
    /**
     * 总射击次数
     */
    private Integer totalShots;
    
    /**
     * 最后射击时间
     */
    private LocalDateTime lastShotTime;
} 