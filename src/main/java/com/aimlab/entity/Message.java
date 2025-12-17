package com.aimlab.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 站内信消息实体类
 */
@Data
public class Message {
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 发送者ID，NULL表示系统消息
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：SYSTEM-系统通知, APPROVAL-审核通知, MANUAL-手动发送
     */
    private String type;
    
    /**
     * 是否已读：false-未读, true-已读
     */
    private Boolean isRead;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    // 非数据库字段，用于展示
    /**
     * 发送者用户名
     */
    private transient String senderName;
    
    /**
     * 接收者用户名
     */
    private transient String receiverName;
}
