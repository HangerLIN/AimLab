package com.aimlab.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运动员信息实体类
 */
@Data
public class Athlete {
    /**
     * 运动员ID
     */
    private Long id;
    
    /**
     * 关联的用户ID
     */
    private Long userId;
    
    /**
     * 运动员姓名
     */
    private String name;
    
    /**
     * 性别：MALE(男), FEMALE(女), UNKNOWN(未知)
     */
    private String gender;
    
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    
    /**
     * 等级
     */
    private String level;
    
    /**
     * 档案审批状态：PENDING、APPROVED、REJECTED
     */
    private String approvalStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 
