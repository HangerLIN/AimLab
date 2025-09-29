package com.aimlab.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 射击记录实体类
 */
@Data
public class ShootingRecord {
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 记录类型：TRAINING(训练), COMPETITION(比赛)
     */
    private String recordType;
    
    /**
     * 运动员ID
     */
    private Long athleteId;
    
    /**
     * 比赛ID（如果是比赛记录）
     */
    private Integer competitionId;
    
    /**
     * 训练场次ID（如果是训练记录）
     */
    private Long trainingSessionId;
    
    /**
     * 第几轮
     */
    private Integer roundNumber;
    
    /**
     * 第几发
     */
    private Integer shotNumber;
    
    /**
     * X轴坐标 (0-1)
     */
    private BigDecimal x;
    
    /**
     * Y轴坐标 (0-1)
     */
    private BigDecimal y;
    
    /**
     * 环数
     */
    private BigDecimal score;
    
    /**
     * 射击时间
     */
    private LocalDateTime shotAt;
    
    /**
     * 用户ID（用于分片）
     */
    private Long userId;
} 