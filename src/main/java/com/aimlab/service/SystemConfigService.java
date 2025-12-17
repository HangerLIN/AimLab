package com.aimlab.service;

import com.aimlab.entity.SystemConfig;
import com.aimlab.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务
 */
@Service
public class SystemConfigService {
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    // 默认配置键
    public static final String KEY_SYSTEM_NAME = "system_name";
    public static final String KEY_SYSTEM_LOGO = "system_logo";
    public static final String KEY_CONTACT_EMAIL = "contact_email";
    public static final String KEY_CONTACT_PHONE = "contact_phone";
    public static final String KEY_SYSTEM_ANNOUNCEMENT = "system_announcement";
    public static final String KEY_MAINTENANCE_MODE = "maintenance_mode";
    
    /**
     * 初始化默认配置
     */
    @PostConstruct
    public void initDefaultConfigs() {
        // 检查并初始化默认配置
        initConfigIfNotExists(KEY_SYSTEM_NAME, "射击训练管理系统", "系统名称");
        initConfigIfNotExists(KEY_SYSTEM_LOGO, "", "系统Logo路径");
        initConfigIfNotExists(KEY_CONTACT_EMAIL, "", "联系邮箱");
        initConfigIfNotExists(KEY_CONTACT_PHONE, "", "联系电话");
        initConfigIfNotExists(KEY_SYSTEM_ANNOUNCEMENT, "", "系统公告");
        initConfigIfNotExists(KEY_MAINTENANCE_MODE, "false", "维护模式");
    }
    
    /**
     * 如果配置不存在则初始化
     */
    private void initConfigIfNotExists(String key, String defaultValue, String description) {
        SystemConfig config = systemConfigMapper.findByKey(key);
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(defaultValue);
            config.setDescription(description);
            systemConfigMapper.insert(config);
        }
    }
    
    /**
     * 获取所有配置（以Map形式返回）
     */
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = systemConfigMapper.findAll();
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }
    
    /**
     * 获取基本设置
     */
    public Map<String, Object> getBasicSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("systemName", getConfigValue(KEY_SYSTEM_NAME, "射击训练管理系统"));
        settings.put("systemLogo", getConfigValue(KEY_SYSTEM_LOGO, ""));
        settings.put("contactEmail", getConfigValue(KEY_CONTACT_EMAIL, ""));
        settings.put("contactPhone", getConfigValue(KEY_CONTACT_PHONE, ""));
        settings.put("systemAnnouncement", getConfigValue(KEY_SYSTEM_ANNOUNCEMENT, ""));
        settings.put("maintenanceMode", "true".equals(getConfigValue(KEY_MAINTENANCE_MODE, "false")));
        return settings;
    }
    
    /**
     * 获取单个配置值
     */
    public String getConfigValue(String key, String defaultValue) {
        SystemConfig config = systemConfigMapper.findByKey(key);
        return config != null ? config.getConfigValue() : defaultValue;
    }
    
    /**
     * 获取单个配置值
     */
    public String getConfigValue(String key) {
        return getConfigValue(key, null);
    }
    
    /**
     * 更新单个配置
     */
    @Transactional
    public boolean updateConfig(String key, String value) {
        SystemConfig config = systemConfigMapper.findByKey(key);
        if (config == null) {
            // 如果不存在则创建
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription("");
            return systemConfigMapper.insert(config) > 0;
        } else {
            return systemConfigMapper.updateByKey(key, value) > 0;
        }
    }
    
    /**
     * 批量更新基本设置
     */
    @Transactional
    public boolean updateBasicSettings(Map<String, Object> settings) {
        if (settings.containsKey("systemName")) {
            updateConfig(KEY_SYSTEM_NAME, String.valueOf(settings.get("systemName")));
        }
        if (settings.containsKey("systemLogo")) {
            updateConfig(KEY_SYSTEM_LOGO, String.valueOf(settings.get("systemLogo")));
        }
        if (settings.containsKey("contactEmail")) {
            updateConfig(KEY_CONTACT_EMAIL, String.valueOf(settings.get("contactEmail")));
        }
        if (settings.containsKey("contactPhone")) {
            updateConfig(KEY_CONTACT_PHONE, String.valueOf(settings.get("contactPhone")));
        }
        if (settings.containsKey("systemAnnouncement")) {
            updateConfig(KEY_SYSTEM_ANNOUNCEMENT, String.valueOf(settings.get("systemAnnouncement")));
        }
        if (settings.containsKey("maintenanceMode")) {
            Object mode = settings.get("maintenanceMode");
            String value = (mode instanceof Boolean) ? mode.toString() : String.valueOf(mode);
            updateConfig(KEY_MAINTENANCE_MODE, value);
        }
        return true;
    }
    
    /**
     * 检查是否处于维护模式
     */
    public boolean isMaintenanceMode() {
        return "true".equals(getConfigValue(KEY_MAINTENANCE_MODE, "false"));
    }
}
