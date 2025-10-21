package com.aimlab.service;

import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.dto.AdminUserDTO;
import com.aimlab.dto.PageResult;
import com.aimlab.entity.User;
import com.aimlab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 管理员用户管理服务
 */
@Service
public class AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 分页查询用户
     *
     * @param username 用户名关键字
     * @param role     角色
     * @param status   状态
     * @param page     页码（从1开始）
     * @param size     每页数量
     * @return 分页结果
     */
    public PageResult<AdminUserDTO> listUsers(String username, String role, Integer status, int page, int size) {
        int pageSize = size <= 0 ? 20 : size;
        int pageIndex = Math.max(page, 1);
        int offset = (pageIndex - 1) * pageSize;

        List<AdminUserDTO> items = userMapper.findAdminUsers(username, role, status, offset, pageSize);
        long total = userMapper.countAdminUsers(username, role, status);
        return new PageResult<>(total, items);
    }

    /**
     * 创建用户
     */
    @Transactional
    public Long createUser(String username, String password, String role, Integer status) {
        User existingUser = userMapper.findByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role != null ? role.toUpperCase(Locale.ROOT) : "ATHLETE");
        user.setStatus(status == null ? 1 : status);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setLastLoginAt(null);
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 更新用户状态
     */
    @Transactional
    public void updateStatus(Long userId, Integer status) {
        if (status == null) {
            throw new RuntimeException("状态不能为空");
        }
        if (userMapper.updateStatus(userId, status) == 0) {
            throw new RuntimeException("更新用户状态失败");
        }
        if (status == 0) {
            StpUtil.logout(userId);
        }
    }

    /**
     * 更新用户角色
     */
    @Transactional
    public void updateRole(Long userId, String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new RuntimeException("角色不能为空");
        }
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        if (userMapper.updateRole(userId, normalizedRole) == 0) {
            throw new RuntimeException("更新用户角色失败");
        }
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        String encoded = passwordEncoder.encode(newPassword);
        if (userMapper.updatePassword(userId, encoded) == 0) {
            throw new RuntimeException("重置密码失败");
        }
        StpUtil.logout(userId);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        StpUtil.logout(userId);
        if (userMapper.deleteById(userId) == 0) {
            throw new RuntimeException("删除用户失败");
        }
    }

    /**
     * 踢出用户在线会话
     */
    public void forceLogout(Long userId) {
        StpUtil.logout(userId);
    }
}
