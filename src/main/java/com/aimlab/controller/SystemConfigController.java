package com.aimlab.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.aimlab.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/admin/settings")
@Tag(name = "系统配置", description = "系统配置管理接口")
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    /**
     * 获取基本设置
     */
    @GetMapping("/basic")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @Operation(summary = "获取基本设置")
    public Map<String, Object> getBasicSettings() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", systemConfigService.getBasicSettings());
        return result;
    }
    
    /**
     * 更新基本设置
     */
    @PutMapping("/basic")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @Operation(summary = "更新基本设置")
    public Map<String, Object> updateBasicSettings(@RequestBody Map<String, Object> settings) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = systemConfigService.updateBasicSettings(settings);
            result.put("success", success);
            result.put("message", success ? "设置已保存" : "保存失败");
            if (success) {
                result.put("data", systemConfigService.getBasicSettings());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存失败: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * 上传系统Logo（存储为Base64到数据库）
     */
    @PostMapping("/logo")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @Operation(summary = "上传系统Logo")
    public Map<String, Object> uploadLogo(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择要上传的文件");
            return result;
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            result.put("success", false);
            result.put("message", "只支持 PNG 和 JPG 格式的图片");
            return result;
        }
        
        // 验证文件大小 (最大16MB)
        if (file.getSize() > 16 * 1024 * 1024) {
            result.put("success", false);
            result.put("message", "文件大小不能超过 16MB");
            return result;
        }
        
        try {
            // 将图片转换为Base64
            byte[] bytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            
            // 构建Data URL格式: data:image/png;base64,xxxxx
            String dataUrl = "data:" + contentType + ";base64," + base64Image;
            
            // 存储到数据库
            systemConfigService.updateConfig(SystemConfigService.KEY_SYSTEM_LOGO, dataUrl);
            
            result.put("success", true);
            result.put("message", "Logo上传成功");
            result.put("url", dataUrl);
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "文件上传失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取单个配置值（公开接口，用于前端获取系统名称等）
     */
    @GetMapping("/public/{key}")
    @Operation(summary = "获取公开配置")
    public Map<String, Object> getPublicConfig(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        
        // 只允许获取部分公开配置
        if ("system_name".equals(key) || "system_logo".equals(key) || 
            "system_announcement".equals(key) || "maintenance_mode".equals(key)) {
            String value = systemConfigService.getConfigValue(key);
            result.put("success", true);
            result.put("key", key);
            result.put("value", value);
        } else {
            result.put("success", false);
            result.put("message", "无权访问此配置");
        }
        
        return result;
    }
    
    /**
     * 获取公开的基本设置（无需登录）
     */
    @GetMapping("/public/basic")
    @Operation(summary = "获取公开的基本设置")
    public Map<String, Object> getPublicBasicSettings() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        data.put("systemName", systemConfigService.getConfigValue(SystemConfigService.KEY_SYSTEM_NAME, "射击训练管理系统"));
        data.put("systemLogo", systemConfigService.getConfigValue(SystemConfigService.KEY_SYSTEM_LOGO, ""));
        data.put("contactEmail", systemConfigService.getConfigValue(SystemConfigService.KEY_CONTACT_EMAIL, ""));
        data.put("contactPhone", systemConfigService.getConfigValue(SystemConfigService.KEY_CONTACT_PHONE, ""));
        data.put("systemAnnouncement", systemConfigService.getConfigValue(SystemConfigService.KEY_SYSTEM_ANNOUNCEMENT, ""));
        data.put("maintenanceMode", "true".equals(systemConfigService.getConfigValue(SystemConfigService.KEY_MAINTENANCE_MODE, "false")));
        
        result.put("success", true);
        result.put("data", data);
        return result;
    }
}
