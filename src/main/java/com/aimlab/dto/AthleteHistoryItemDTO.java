package com.aimlab.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运动员历史记录项数据传输对象
 * 用于表示单次训练或比赛的摘要
 */
@Data
public class AthleteHistoryItemDTO {
    
    /**
     * 记录类型：TRAINING(训练), COMPETITION(比赛)
     */
    private String type;
    
    /**
     * 记录ID（训练场次ID或比赛ID）
     */
    private Long id;
    
    /**
     * 记录名称（训练名称或比赛名称）
     */
    private String name;
    
    /**
     * 记录日期（训练开始时间或比赛开始时间）
     */
    private LocalDateTime date;
    
    /**
     * 结果（训练平均分或比赛最终排名）
     */
    private String result;
    
    /**
     * 结果数值（训练平均分或比赛最终分数）
     */
    private BigDecimal resultValue;
    
    /**
     * 比赛排名（仅对比赛有效）
     */
    private Integer rank;
} 