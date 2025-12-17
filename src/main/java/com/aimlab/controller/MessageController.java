package com.aimlab.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.entity.Message;
import com.aimlab.entity.User;
import com.aimlab.mapper.UserMapper;
import com.aimlab.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内信消息控制器
 */
@RestController
@RequestMapping("/api/messages")
@Tag(name = "站内信", description = "站内信消息管理接口")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 获取当前用户的消息列表
     */
    @GetMapping
    @SaCheckLogin
    @Operation(summary = "获取消息列表")
    public Map<String, Object> getMessageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> data = messageService.getMessageList(userId, type, isRead, page, size);
            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取消息列表失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    @SaCheckLogin
    @Operation(summary = "获取未读消息数量")
    public Map<String, Object> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            int count = messageService.getUnreadCount(userId);
            result.put("success", true);
            result.put("count", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取未读数量失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取消息详情
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "获取消息详情")
    public Map<String, Object> getMessageDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            Message message = messageService.getMessageById(id);
            if (message == null) {
                result.put("success", false);
                result.put("message", "消息不存在");
                return result;
            }
            
            // 验证权限：只能查看自己的消息
            if (!message.getReceiverId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权查看此消息");
                return result;
            }
            
            // 自动标记为已读
            if (!Boolean.TRUE.equals(message.getIsRead())) {
                messageService.markAsRead(id, userId);
                message.setIsRead(true);
            }
            
            result.put("success", true);
            result.put("data", message);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取消息详情失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 标记消息为已读
     */
    @PutMapping("/{id}/read")
    @SaCheckLogin
    @Operation(summary = "标记消息为已读")
    public Map<String, Object> markAsRead(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = messageService.markAsRead(id, userId);
            result.put("success", success);
            if (!success) {
                result.put("message", "标记失败，消息不存在或无权操作");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "标记失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 标记所有消息为已读
     */
    @PutMapping("/read-all")
    @SaCheckLogin
    @Operation(summary = "标记所有消息为已读")
    public Map<String, Object> markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            int count = messageService.markAllAsRead(userId);
            result.put("success", true);
            result.put("count", count);
            result.put("message", "已将 " + count + " 条消息标记为已读");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "删除消息")
    public Map<String, Object> deleteMessage(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = messageService.deleteMessage(id, userId);
            result.put("success", success);
            if (!success) {
                result.put("message", "删除失败，消息不存在或无权操作");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 批量删除消息
     */
    @DeleteMapping("/batch")
    @SaCheckLogin
    @Operation(summary = "批量删除消息")
    public Map<String, Object> deleteMessages(@RequestBody Map<String, List<Long>> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Long> ids = body.get("ids");
            int count = messageService.deleteMessages(ids, userId);
            result.put("success", true);
            result.put("count", count);
            result.put("message", "已删除 " + count + " 条消息");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        
        return result;
    }
    
    // ============ 管理员接口 ============
    
    /**
     * 管理员发送消息给指定用户
     */
    @PostMapping("/admin/send")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @Operation(summary = "管理员发送消息")
    public Map<String, Object> adminSendMessage(@RequestBody Map<String, Object> body) {
        Long senderId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long receiverId = Long.valueOf(body.get("receiverId").toString());
            String title = (String) body.get("title");
            String content = (String) body.get("content");
            
            if (title == null || title.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "消息标题不能为空");
                return result;
            }
            
            if (content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "消息内容不能为空");
                return result;
            }
            
            Long messageId = messageService.sendManualMessage(senderId, receiverId, title, content);
            result.put("success", true);
            result.put("messageId", messageId);
            result.put("message", "消息发送成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发送失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 管理员批量发送消息
     */
    @PostMapping("/admin/send-batch")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @Operation(summary = "管理员批量发送消息")
    public Map<String, Object> adminSendBatchMessage(@RequestBody Map<String, Object> body) {
        Long senderId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> receiverIds = (List<Long>) body.get("receiverIds");
            String title = (String) body.get("title");
            String content = (String) body.get("content");
            
            if (receiverIds == null || receiverIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "接收者列表不能为空");
                return result;
            }
            
            if (title == null || title.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "消息标题不能为空");
                return result;
            }
            
            if (content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "消息内容不能为空");
                return result;
            }
            
            messageService.sendBatchMessage(senderId, receiverIds, title, content, MessageService.TYPE_MANUAL);
            result.put("success", true);
            result.put("count", receiverIds.size());
            result.put("message", "已向 " + receiverIds.size() + " 位用户发送消息");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发送失败: " + e.getMessage());
        }
        
        return result;
    }
    
    // ============ 用户发送消息接口 ============
    
    /**
     * 用户发送消息给管理员
     */
    @PostMapping("/send-to-admin")
    @SaCheckLogin
    @Operation(summary = "发送消息给管理员")
    public Map<String, Object> sendToAdmin(@RequestBody Map<String, Object> body) {
        Long senderId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = new HashMap<>();
        
        try {
            String title = (String) body.get("title");
            String content = (String) body.get("content");
            
            if (title == null || title.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "消息标题不能为空");
                return result;
            }
            
            if (content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "消息内容不能为空");
                return result;
            }
            
            // 获取所有管理员
            List<User> admins = userMapper.findByRole("ADMIN");
            if (admins == null || admins.isEmpty()) {
                result.put("success", false);
                result.put("message", "暂无管理员可接收消息");
                return result;
            }
            
            // 获取发送者信息
            User sender = userMapper.findById(senderId);
            String senderName = sender != null ? sender.getUsername() : "用户";
            
            // 向所有管理员发送消息
            String fullTitle = "【用户反馈】" + title;
            String fullContent = "来自用户 " + senderName + " 的消息：\n\n" + content;
            
            for (User admin : admins) {
                messageService.sendMessage(senderId, admin.getId(), fullTitle, fullContent, MessageService.TYPE_MANUAL);
            }
            
            result.put("success", true);
            result.put("message", "消息已发送给管理员");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发送失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取可发送消息的用户列表（管理员使用）
     */
    @GetMapping("/admin/users")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @Operation(summary = "获取可发送消息的用户列表")
    public Map<String, Object> getMessageableUsers(
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<User> users = userMapper.findAll();
            List<Map<String, Object>> userList = new ArrayList<>();
            
            for (User user : users) {
                // 如果有关键字，过滤用户名
                if (keyword != null && !keyword.isEmpty()) {
                    if (!user.getUsername().toLowerCase().contains(keyword.toLowerCase())) {
                        continue;
                    }
                }
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("role", user.getRole());
                userList.add(userInfo);
            }
            
            result.put("success", true);
            result.put("data", userList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户列表失败: " + e.getMessage());
        }
        
        return result;
    }
}
