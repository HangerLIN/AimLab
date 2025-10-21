package com.aimlab.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 训练场次实体类
 */
@Data
public class TrainingSession {
    /**
     * 训练场次ID
     */
    private Long id;
    
    /**
     * 运动员ID
     */
    private Long athleteId;
    
    /**
     * 训练名称
     */
    private String sessionName;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 训练备注
     */
    private String notes;

    /**
     * 训练项目/科目
     */
    private String projectType;

    /**
     * 报告生成时间
     */
    private LocalDateTime reportGeneratedAt;
}
