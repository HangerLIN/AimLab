package com.aimlab.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 比赛运动员关联实体类
 */
@Data
public class CompetitionAthlete {
    /**
     * 关联ID
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
     * 报名状态：ENROLLED(已报名), CONFIRMED(已确认), REJECTED(已拒绝), WITHDRAWN(已退赛)
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 报名时间
     */
    private LocalDateTime registeredAt;
    
    /**
     * 运动员姓名（非数据库字段，用于展示）
     */
    private String athleteName;
} 