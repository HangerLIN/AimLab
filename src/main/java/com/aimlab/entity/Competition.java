package com.aimlab.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 比赛信息实体类
 */
@Data
public class Competition {
    /**
     * 比赛ID
     */
    private Integer id;
    
    /**
     * 比赛名称
     */
    private String name;
    
    /**
     * 比赛描述
     */
    private String description;
    
    /**
     * 赛制类型
     */
    private String formatType;
    
    /**
     * 总轮数
     */
    private Integer roundsCount;
    
    /**
     * 每轮射击次数
     */
    private Integer shotsPerRound;
    
    /**
     * 每发时间限制(秒)
     */
    private Integer timeLimitPerShot;
    
    /**
     * 报名开始时间
     */
    private LocalDateTime enrollStartAt;
    
    /**
     * 报名结束时间
     */
    private LocalDateTime enrollEndAt;
    
    /**
     * 报名权限等级
     */
    private String accessLevel;
    
    /**
     * 比赛状态：CREATED(已创建), RUNNING(进行中), PAUSED(已暂停), COMPLETED(已完成), CANCELED(已取消)
     */
    private String status;
    
    /**
     * 创建者ID，关联users表
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 开始时间
     */
    private LocalDateTime startedAt;
    
    /**
     * 结束时间
     */
    private LocalDateTime endedAt;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
    
    /**
     * 比赛持续时间(秒)
     */
    private Integer durationSeconds;
} 
