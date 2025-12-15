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
     * @return 注册成功返回用户ID
     * @throws RuntimeException 当验证失败时
     */
    @Transactional
    public Long register(User user) {
        // 验证用户名是否为空
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        
        // 验证密码是否为空
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        
        // 验证用户名长度
        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            throw new RuntimeException("用户名长度应在3-20个字符之间");
        }
        
        // 验证密码长度
        if (user.getPassword().length() < 6) {
            throw new RuntimeException("密码长度至少为6个字符");
        }
        
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在，请更换用户名");
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
     * @return 登录成功返回token信息，失败抛出异常
     */
    public SaTokenInfo login(String username, String password) {
        // 验证用户名是否为空
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        
        // 验证密码是否为空
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        
        // 根据用户名查询用户
        User user = userMapper.findByUsername(username);
        
        // 用户不存在
        if (user == null) {
            throw new RuntimeException("用户不存在，请先注册");
        }
        
        // 密码不匹配
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误，请重新输入");
        }
        
        // 账号被禁用
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
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
