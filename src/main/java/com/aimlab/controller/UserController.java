package com.aimlab.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.entity.Athlete;
import com.aimlab.entity.User;
import com.aimlab.service.UserService;
import com.aimlab.service.AthleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户注册和登录相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AthleteService athleteService;
    
    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @return 注册结果
     */
    @Operation(summary = "用户注册", description = "创建新用户账号")
    @ApiResponse(responseCode = "200", description = "用户注册成功")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            Long userId = userService.register(user);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("userId", userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 用户登录
     * 
     * @param loginData 登录数据
     * @return 登录结果
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    @ApiResponse(responseCode = "200", description = "登录成功，返回token信息")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            
            if (username == null || password == null) {
                throw new IllegalArgumentException("用户名和密码不能为空");
            }
            
            SaTokenInfo tokenInfo = userService.login(username, password);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("tokenInfo", tokenInfo);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的基本信息和关联的运动员ID")
    @ApiResponse(responseCode = "200", description = "成功获取用户信息")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userService.getUserById(userId);
            
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(result);
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("updatedAt", user.getUpdatedAt());
            
            Athlete athlete = athleteService.getAthleteByUserId(user.getId());
            if (athlete != null) {
                userInfo.put("athleteId", athlete.getId());
                userInfo.put("athleteName", athlete.getName());
            }
            
            result.put("success", true);
            result.put("user", userInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
