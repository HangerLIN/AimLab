package com.aimlab.config;

import cn.dev33.satoken.stp.StpInterface;
import com.aimlab.entity.User;
import com.aimlab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }
    
    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 获取用户ID
        Long userId = Long.parseLong(loginId.toString());
        
        // 查询用户角色
        User user = userMapper.findById(userId);
        if (user != null && user.getRole() != null) {
            return Arrays.asList(user.getRole());
        }
        
        return new ArrayList<>();
    }
}
