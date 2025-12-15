package com.aimlab.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.aimlab.entity.User;
import com.aimlab.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册和登录相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    /**
     * 健康检查端点
     * 
     * @return 服务状态
     */
    @Operation(summary = "服务健康检查", description = "检查服务是否正常运行")
    @ApiResponse(responseCode = "200", description = "服务正常")
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "服务正常运行");
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }
    
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
            // 字段验证
            Map<String, String> fieldErrors = validateRegisterForm(user);
            if (!fieldErrors.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "表单验证失败");
                response.put("fieldErrors", fieldErrors);
                return ResponseEntity.badRequest().body(response);
            }
            
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
            error.put("errorCode", "REGISTER_FAILED");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 用户登录
     * 
     * @param loginData 登录数据，包含username和password
     * @return 登录结果
     */
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    @ApiResponse(responseCode = "200", description = "登录成功，返回token信息")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            
            // 字段级别的验证
            Map<String, String> fieldErrors = validateLoginForm(username, password);
            if (!fieldErrors.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "表单验证失败");
                response.put("fieldErrors", fieldErrors);
                return ResponseEntity.badRequest().body(response);
            }
            
            SaTokenInfo tokenInfo = userService.login(username, password);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("tokenInfo", tokenInfo);
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            // 根据错误消息设置错误代码
            String message = e.getMessage();
            if (message.contains("用户不存在")) {
                error.put("errorCode", "USER_NOT_FOUND");
            } else if (message.contains("密码错误")) {
                error.put("errorCode", "WRONG_PASSWORD");
            } else if (message.contains("禁用")) {
                error.put("errorCode", "ACCOUNT_DISABLED");
            } else {
                error.put("errorCode", "LOGIN_FAILED");
            }
            
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "登录失败，请重试");
            error.put("errorCode", "SERVER_ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 验证登录表单
     * 
     * @param username 用户名
     * @param password 密码
     * @return 错误映射，如果为空表示验证通过
     */
    private Map<String, String> validateLoginForm(String username, String password) {
        Map<String, String> errors = new HashMap<>();
        
        if (username == null || username.trim().isEmpty()) {
            errors.put("username", "用户名不能为空");
        }
        
        if (password == null || password.trim().isEmpty()) {
            errors.put("password", "密码不能为空");
        }
        
        return errors;
    }
    
    /**
     * 验证注册表单
     * 
     * @param user 用户对象
     * @return 错误映射，如果为空表示验证通过
     */
    private Map<String, String> validateRegisterForm(User user) {
        Map<String, String> errors = new HashMap<>();
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            errors.put("username", "用户名不能为空");
        } else if (user.getUsername().length() < 3) {
            errors.put("username", "用户名长度至少为3个字符");
        } else if (user.getUsername().length() > 20) {
            errors.put("username", "用户名长度不能超过20个字符");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            errors.put("password", "密码不能为空");
        } else if (user.getPassword().length() < 6) {
            errors.put("password", "密码长度至少为6个字符");
        }
        
        return errors;
    }
} 