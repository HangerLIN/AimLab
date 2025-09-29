package com.aimlab.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 */
@Data
public class User {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码（加密后）
     */
    private String password;
    
    /**
     * 角色：ATHLETE(运动员), COACH(教练), ADMIN(管理员)
     */
    private String role;
    
    /**
     * 账号状态：1-正常, 0-禁用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 