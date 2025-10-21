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
        Long userId = Long.parseLong(loginId.toString());
        User user = userMapper.findById(userId);
        if (user == null || user.getRole() == null) {
            return new ArrayList<>();
        }

        List<String> permissions = new ArrayList<>();
        switch (user.getRole()) {
            case "ADMIN":
                permissions.add("admin:dashboard");
                permissions.add("admin:users");
                permissions.add("admin:users:manage");
                permissions.add("admin:athletes");
                permissions.add("admin:athletes:export");
                permissions.add("admin:athletes:import");
                permissions.add("admin:reports");
                permissions.add("admin:training.analytics");
                permissions.add("admin:training.export");
                permissions.add("admin:competitions.manage");
                permissions.add("admin:competitions.export");
                permissions.add("competition:force-finish");
                break;
            case "COACH":
                permissions.add("coach:training");
                break;
            case "ATHLETE":
            default:
                permissions.add("athlete:self");
                break;
        }
        return permissions;
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
