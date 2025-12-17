package com.aimlab.service;

import com.aimlab.entity.Message;
import com.aimlab.entity.User;
import com.aimlab.mapper.MessageMapper;
import com.aimlab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内信消息服务
 */
@Service
public class MessageService {
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    // 消息类型常量
    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_APPROVAL = "APPROVAL";
    public static final String TYPE_MANUAL = "MANUAL";
    
    /**
     * 发送消息
     * @param senderId 发送者ID（系统消息为null）
     * @param receiverId 接收者ID
     * @param title 标题
     * @param content 内容
     * @param type 消息类型
     * @return 消息ID
     */
    @Transactional
    public Long sendMessage(Long senderId, Long receiverId, String title, String content, String type) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type != null ? type : TYPE_SYSTEM);
        message.setIsRead(false);
        
        messageMapper.insert(message);
        return message.getId();
    }
    
    /**
     * 发送系统通知
     */
    public Long sendSystemNotification(Long receiverId, String title, String content) {
        return sendMessage(null, receiverId, title, content, TYPE_SYSTEM);
    }
    
    /**
     * 发送审核通知
     */
    public Long sendApprovalNotification(Long receiverId, String title, String content) {
        return sendMessage(null, receiverId, title, content, TYPE_APPROVAL);
    }
    
    /**
     * 管理员手动发送消息
     */
    public Long sendManualMessage(Long senderId, Long receiverId, String title, String content) {
        return sendMessage(senderId, receiverId, title, content, TYPE_MANUAL);
    }
    
    /**
     * 批量发送消息给多个用户
     */
    @Transactional
    public void sendBatchMessage(Long senderId, List<Long> receiverIds, String title, String content, String type) {
        for (Long receiverId : receiverIds) {
            sendMessage(senderId, receiverId, title, content, type);
        }
    }
    
    /**
     * 获取消息详情
     */
    public Message getMessageById(Long id) {
        return messageMapper.findById(id);
    }
    
    /**
     * 获取用户的消息列表（分页）
     */
    public Map<String, Object> getMessageList(Long receiverId, String type, Boolean isRead, int page, int size) {
        int offset = (page - 1) * size;
        List<Message> messages = messageMapper.findByReceiverId(receiverId, type, isRead, offset, size);
        int total = messageMapper.countByReceiverId(receiverId, type, isRead);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", messages);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        return result;
    }
    
    /**
     * 获取用户未读消息数量
     */
    public int getUnreadCount(Long receiverId) {
        return messageMapper.countUnreadByReceiverId(receiverId);
    }
    
    /**
     * 标记消息为已读
     */
    @Transactional
    public boolean markAsRead(Long messageId, Long userId) {
        Message message = messageMapper.findById(messageId);
        if (message == null || !message.getReceiverId().equals(userId)) {
            return false;
        }
        return messageMapper.markAsRead(messageId) > 0;
    }
    
    /**
     * 标记所有消息为已读
     */
    @Transactional
    public int markAllAsRead(Long userId) {
        return messageMapper.markAllAsRead(userId);
    }
    
    /**
     * 删除消息
     */
    @Transactional
    public boolean deleteMessage(Long messageId, Long userId) {
        Message message = messageMapper.findById(messageId);
        if (message == null || !message.getReceiverId().equals(userId)) {
            return false;
        }
        return messageMapper.deleteById(messageId) > 0;
    }
    
    /**
     * 批量删除消息
     */
    @Transactional
    public int deleteMessages(List<Long> messageIds, Long userId) {
        if (messageIds == null || messageIds.isEmpty()) {
            return 0;
        }
        return messageMapper.deleteByIds(messageIds, userId);
    }
    
    // ============ 审核通知相关方法 ============
    
    /**
     * 发送运动员档案审核通过通知
     */
    public void sendAthleteApprovalNotification(Long userId, String athleteName) {
        String title = "档案审核通过通知";
        String content = String.format(
            "尊敬的 %s：\n\n" +
            "恭喜您！您的运动员档案已通过管理员审核。\n\n" +
            "现在您可以：\n" +
            "• 参加训练和比赛\n" +
            "• 查看和管理您的训练记录\n" +
            "• 参与数据分析和统计\n\n" +
            "祝您训练顺利！\n\n" +
            "—— 射击训练管理系统",
            athleteName
        );
        sendApprovalNotification(userId, title, content);
    }
    
    /**
     * 发送运动员档案审核拒绝通知
     */
    public void sendAthleteRejectionNotification(Long userId, String athleteName, String reason) {
        String title = "档案审核未通过通知";
        String content = String.format(
            "尊敬的 %s：\n\n" +
            "很抱歉，您的运动员档案未通过审核。\n\n" +
            "拒绝原因：%s\n\n" +
            "请根据以上原因修改您的档案信息后重新提交。\n" +
            "如有疑问，请联系管理员。\n\n" +
            "—— 射击训练管理系统",
            athleteName,
            reason != null ? reason : "未提供具体原因"
        );
        sendApprovalNotification(userId, title, content);
    }
    
    /**
     * 发送档案修改审核通过通知
     */
    public void sendModificationApprovalNotification(Long userId, String athleteName) {
        String title = "档案修改审核通过通知";
        String content = String.format(
            "尊敬的 %s：\n\n" +
            "您提交的档案修改申请已通过管理员审核。\n" +
            "您的档案信息已更新为最新内容。\n\n" +
            "—— 射击训练管理系统",
            athleteName
        );
        sendApprovalNotification(userId, title, content);
    }
    
    /**
     * 发送档案修改审核拒绝通知
     */
    public void sendModificationRejectionNotification(Long userId, String athleteName, String reason) {
        String title = "档案修改审核未通过通知";
        String content = String.format(
            "尊敬的 %s：\n\n" +
            "很抱歉，您提交的档案修改申请未通过审核。\n\n" +
            "拒绝原因：%s\n\n" +
            "如有疑问，请联系管理员。\n\n" +
            "—— 射击训练管理系统",
            athleteName,
            reason != null ? reason : "未提供具体原因"
        );
        sendApprovalNotification(userId, title, content);
    }
    
    // ============ 通知管理员相关方法 ============
    
    /**
     * 通知所有管理员：新运动员注册待审核
     */
    public void notifyAdminsNewAthleteRegistration(String athleteName) {
        List<User> admins = userMapper.findByRole("ADMIN");
        if (admins == null || admins.isEmpty()) {
            return;
        }
        
        String title = "新运动员注册待审核";
        String content = String.format(
            "有新的运动员注册申请需要审核：\n\n" +
            "运动员姓名：%s\n\n" +
            "请前往【运动员管理】页面进行审核。\n\n" +
            "—— 射击训练管理系统",
            athleteName
        );
        
        for (User admin : admins) {
            sendApprovalNotification(admin.getId(), title, content);
        }
    }
    
    /**
     * 通知所有管理员：运动员档案修改待审核
     */
    public void notifyAdminsAthleteModification(String athleteName) {
        List<User> admins = userMapper.findByRole("ADMIN");
        if (admins == null || admins.isEmpty()) {
            return;
        }
        
        String title = "运动员档案修改待审核";
        String content = String.format(
            "有运动员档案修改申请需要审核：\n\n" +
            "运动员姓名：%s\n\n" +
            "请前往【运动员管理】页面进行审核。\n\n" +
            "—— 射击训练管理系统",
            athleteName
        );
        
        for (User admin : admins) {
            sendApprovalNotification(admin.getId(), title, content);
        }
    }
}
