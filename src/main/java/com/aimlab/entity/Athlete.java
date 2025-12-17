package com.aimlab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
     * 等级：国家级、省级、市级、业余
     */
    private String level;
    
    /**
     * 头像二进制数据（MEDIUMBLOB存储）
     * 使用@JsonIgnore避免在JSON序列化时返回大量二进制数据
     */
    @JsonIgnore
    private byte[] avatarData;
    
    /**
     * 头像MIME类型（如 image/png, image/jpeg）
     */
    @JsonIgnore
    private String avatarType;
    
    /**
     * 是否有头像（用于前端判断）
     */
    public boolean isHasAvatar() {
        return avatarData != null && avatarData.length > 0;
    }
    
    /**
     * 档案审批状态：PENDING、APPROVED、REJECTED
     * PENDING - 初始审批状态或档案修改待审批
     * APPROVED - 已批准，可以参与比赛和训练
     * REJECTED - 已拒绝
     */
    private String approvalStatus;
    
    /**
     * 档案修改审批状态：用于标记对已批准档案的修改
     * null/PENDING - 无修改或等待审批
     * APPROVED - 修改已批准
     */
    private String modificationStatus;
    
    /**
     * 待审批的修改数据（JSON格式）
     * 当运动员提交档案修改时，新数据暂存在这里，等待管理员审批
     */
    private String pendingModificationData;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 
