package com.aimlab.mapper;

import com.aimlab.dto.AdminUserDTO;
import com.aimlab.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 统计用户总数
     *
     * @return 用户数量
     */
    long countAll();

    /**
     * 根据角色统计用户数量
     *
     * @param role 角色标识
     * @return 用户数量
     */
    long countByRole(@Param("role") String role);

    /**
     * 根据筛选条件查询用户（包含运动员关联信息）
     *
     * @param username 用户名关键字
     * @param role     角色
     * @param status   状态
     * @param offset   分页偏移
     * @param limit    分页数量
     * @return 用户列表
     */
    List<AdminUserDTO> findAdminUsers(@Param("username") String username,
                                      @Param("role") String role,
                                      @Param("status") Integer status,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    /**
     * 根据条件统计用户数量
     *
     * @param username 用户名
     * @param role 角色
     * @param status 状态
     * @return 数量
     */
    long countAdminUsers(@Param("username") String username,
                         @Param("role") String role,
                         @Param("status") Integer status);

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新用户角色
     *
     * @param id 用户ID
     * @param role 角色
     * @return 影响行数
     */
    int updateRole(@Param("id") Long id, @Param("role") String role);

    /**
     * 更新用户密码
     *
     * @param id 用户ID
     * @param password 加密后的密码
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新最近登录时间
     *
     * @param id 用户ID
     * @param lastLoginAt 最近登录时间
     * @return 影响行数
     */
    int updateLastLoginAt(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
