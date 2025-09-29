package com.aimlab.mapper;

import com.aimlab.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象，如果不存在则返回null
     */
    User findByUsername(@Param("username") String username);
    
    /**
     * 插入新用户
     * 
     * @param user 用户对象
     * @return 影响的行数
     */
    int insert(User user);
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回null
     */
    User findById(@Param("id") Long id);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户对象
     * @return 影响的行数
     */
    int update(User user);
} 