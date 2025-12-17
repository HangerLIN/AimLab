package com.aimlab.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.entity.Athlete;
import com.aimlab.entity.User;
import com.aimlab.mapper.AthleteMapper;
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
    private AthleteMapper athleteMapper;
    
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
    
    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 验证参数
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new RuntimeException("旧密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度至少为6个字符");
        }
        
        // 查询用户
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        
        // 新密码不能与旧密码相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }
        
        // 更新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        int rows = userMapper.updatePassword(userId, encodedPassword);
        
        return rows > 0;
    }
    
    /**
     * 找回密码（通过用户名和真实姓名验证）
     *
     * @param username 用户名
     * @param realName 真实姓名
     * @param newPassword 新密码（可选，如果为空则重置为123456）
     * @return 重置结果信息
     */
    @Transactional
    public String resetPassword(String username, String realName, String newPassword) {
        // 验证用户名
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        
        // 查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 查询关联的运动员信息
        Athlete athlete = athleteMapper.findByUserId(user.getId());
        
        // 验证真实姓名
        if (athlete != null && athlete.getName() != null && !athlete.getName().trim().isEmpty()) {
            // 有真实姓名，需要验证
            if (realName == null || realName.trim().isEmpty()) {
                throw new RuntimeException("请输入真实姓名进行验证");
            }
            if (!athlete.getName().equals(realName.trim())) {
                throw new RuntimeException("真实姓名验证失败");
            }
            
            // 验证通过，设置新密码
            String finalPassword = (newPassword != null && newPassword.length() >= 6) ? newPassword : "123456";
            String encodedPassword = passwordEncoder.encode(finalPassword);
            userMapper.updatePassword(user.getId(), encodedPassword);
            
            if (newPassword != null && newPassword.length() >= 6) {
                return "密码重置成功";
            } else {
                return "密码已重置为默认密码：123456";
            }
        } else {
            // 没有真实姓名，直接重置为123456
            String encodedPassword = passwordEncoder.encode("123456");
            userMapper.updatePassword(user.getId(), encodedPassword);
            return "该账户未设置真实姓名，密码已重置为默认密码：123456";
        }
    }
    
    /**
     * 检查用户是否设置了真实姓名
     *
     * @param username 用户名
     * @return 是否有真实姓名
     */
    public boolean hasRealName(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return false;
        }
        
        Athlete athlete = athleteMapper.findByUserId(user.getId());
        return athlete != null && athlete.getName() != null && !athlete.getName().trim().isEmpty();
    }
}
