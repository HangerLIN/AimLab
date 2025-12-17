package com.aimlab.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@Data
public class SystemConfig {
    /**
     * 配置ID
     */
    private Long id;
    
    /**
     * 配置键名
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
