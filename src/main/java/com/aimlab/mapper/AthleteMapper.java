package com.aimlab.mapper;

import com.aimlab.entity.Athlete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 运动员Mapper接口
 */
@Mapper
public interface AthleteMapper {
    
    /**
     * 插入新运动员信息
     * 
     * @param athlete 运动员对象
     * @return 影响的行数
     */
    int insert(Athlete athlete);
    
    /**
     * 根据ID查询运动员信息
     * 
     * @param id 运动员ID
     * @return 运动员对象，如果不存在则返回null
     */
    Athlete findById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询运动员信息
     * 
     * @param userId 用户ID
     * @return 运动员对象，如果不存在则返回null
     */
    Athlete findByUserId(@Param("userId") Long userId);
    
    /**
     * 更新运动员信息
     * 
     * @param athlete 运动员对象
     * @return 影响的行数
     */
    int update(Athlete athlete);
    
    /**
     * 删除运动员信息
     * 
     * @param id 运动员ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") Long id);
} 