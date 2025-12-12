package com.aimlab.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.entity.User;
import com.aimlab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @return 注册成功返回用户ID，失败返回null
     */
    @Transactional
    public Long register(User user) {
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认角色和状态（如果未指定角色，默认为ATHLETE）
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ATHLETE");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLoginAt(null);
        
        // 保存用户
        userMapper.insert(user);
        
        return user.getId();
    }
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回token信息，失败返回null
     */
    public SaTokenInfo login(String username, String password) {
        // 根据用户名查询用户
        User user = userMapper.findByUsername(username);
        
        // 用户不存在或密码不匹配
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 账号被禁用
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 登录成功，使用Sa-Token生成token
        StpUtil.login(user.getId());

        // 更新最近登录时间
        userMapper.updateLastLoginAt(user.getId(), LocalDateTime.now());
        
        // 返回token信息
        return StpUtil.getTokenInfo();
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户对象
     */
    public User getUserById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.findById(id);
    }
}
