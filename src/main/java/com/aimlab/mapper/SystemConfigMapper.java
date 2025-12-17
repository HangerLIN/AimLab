package com.aimlab.mapper;

import com.aimlab.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统配置Mapper
 */
@Mapper
public interface SystemConfigMapper {
    
    /**
     * 根据配置键获取配置
     */
    SystemConfig findByKey(@Param("configKey") String configKey);
    
    /**
     * 获取所有配置
     */
    List<SystemConfig> findAll();
    
    /**
     * 插入配置
     */
    int insert(SystemConfig config);
    
    /**
     * 更新配置值
     */
    int updateByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
    
    /**
     * 批量更新配置
     */
    int batchUpdate(@Param("configs") List<SystemConfig> configs);
    
    /**
     * 删除配置
     */
    int deleteByKey(@Param("configKey") String configKey);
}
