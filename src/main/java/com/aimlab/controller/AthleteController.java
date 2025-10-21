package com.aimlab.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.dto.AthleteProfileDTO;
import com.aimlab.entity.Athlete;
import com.aimlab.service.AthleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 运动员控制器
 */
@Tag(name = "运动员管理", description = "运动员档案和个人资料相关接口")
@RestController
@RequestMapping("/api/athletes")
public class AthleteController {

    @Autowired
    private AthleteService athleteService;
    
    /**
     * 创建运动员档案
     * 
     * @param athlete 运动员信息
     * @return 创建结果
     */
    @Operation(summary = "创建运动员档案", description = "创建新的运动员档案信息")
    @ApiResponse(responseCode = "200", description = "运动员档案创建成功")
    // @SaCheckLogin  // 测试环境暂时注释
    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(@RequestBody Athlete athlete) {
        try {
            // 检查用户是否已登录
            if (!StpUtil.isLogin()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "请先登录");
                error.put("code", "NOT_LOGIN");
                return ResponseEntity.status(401).body(error);
            }
            
            // 如果未指定userId，则使用当前登录用户的ID
            if (athlete.getUserId() == null) {
                athlete.setUserId(StpUtil.getLoginIdAsLong());
            }
            
            Long athleteId = athleteService.createAthleteProfile(athlete);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "运动员档案创建成功");
            result.put("athleteId", athleteId);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取当前登录用户的运动员档案
     * 
     * @return 运动员信息
     */
    @Operation(summary = "获取当前用户运动员档案", description = "获取当前登录用户的运动员基本信息")
    @ApiResponse(responseCode = "200", description = "成功获取运动员档案")
    // @SaCheckLogin  // 测试环境暂时注释
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            // 检查用户是否已登录
            if (!StpUtil.isLogin()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "请先登录");
                error.put("code", "NOT_LOGIN");
                return ResponseEntity.status(401).body(error);
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            
            if (athlete == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "运动员档案不存在");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("athlete", athlete);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 更新运动员档案
     * 
     * @param athlete 运动员信息
     * @return 更新结果
     */
    @Operation(summary = "更新运动员档案", description = "更新当前登录用户的运动员档案信息")
    @ApiResponse(responseCode = "200", description = "运动员档案更新成功")
    // @SaCheckLogin  // 测试环境暂时注释
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Athlete athlete) {
        try {
            // 检查用户是否已登录
            if (!StpUtil.isLogin()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "请先登录");
                error.put("code", "NOT_LOGIN");
                return ResponseEntity.status(401).body(error);
            }
            
            // 获取当前登录用户的运动员档案
            Long userId = StpUtil.getLoginIdAsLong();
            Athlete existingAthlete = athleteService.getAthleteByUserId(userId);
            
            if (existingAthlete == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "运动员档案不存在");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 设置ID，确保更新的是当前用户的档案
            athlete.setId(existingAthlete.getId());
            athlete.setUserId(userId); // 确保不会修改关联的用户ID
            
            boolean updated = athleteService.updateAthleteProfile(athlete);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", updated);
            result.put("message", updated ? "运动员档案更新成功" : "运动员档案更新失败");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 根据ID获取运动员信息
     * 
     * @param id 运动员ID
     * @return 运动员信息
     */
    @Operation(summary = "根据ID获取运动员信息", description = "通过运动员ID获取基本信息")
    @ApiResponse(responseCode = "200", description = "成功获取运动员信息")
    // @SaCheckLogin  // 测试环境暂时注释
    @GetMapping("/{id}")
    public ResponseEntity<?> getAthleteById(@Parameter(description = "运动员ID") @PathVariable Long id) {
        try {
            Athlete athlete = athleteService.getAthleteById(id);

            if (athlete == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "运动员不存在");
                return ResponseEntity.badRequest().body(error);
            }

            boolean isAdmin = false;
            try {
                isAdmin = StpUtil.hasPermission("admin:athletes");
            } catch (Exception ignored) {
                // 未登录或权限不足时保持默认值
            }

            if (!isAdmin) {
                if (!StpUtil.isLogin()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "请先登录");
                    error.put("code", "NOT_LOGIN");
                    return ResponseEntity.status(401).body(error);
                }
                Long currentUserId = StpUtil.getLoginIdAsLong();
                if (athlete.getUserId() == null || !athlete.getUserId().equals(currentUserId)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "无权访问该运动员信息");
                    return ResponseEntity.status(403).body(error);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("athlete", athlete);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取运动员个人资料（包含历史记录和生涯统计）
     * 
     * @param id 运动员ID
     * @return 运动员个人资料
     */
    @Operation(summary = "获取运动员完整个人资料", description = "获取运动员的详细资料，包含历史记录和生涯统计")
    @ApiResponse(responseCode = "200", description = "成功获取运动员个人资料")
    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getAthleteProfile(@Parameter(description = "运动员ID") @PathVariable Long id) {
        try {
            boolean isAdmin = false;
            try {
                isAdmin = StpUtil.hasPermission("admin:athletes");
            } catch (Exception ignored) {
                // ignore
            }

            if (!isAdmin) {
                if (!StpUtil.isLogin()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "请先登录");
                    error.put("code", "NOT_LOGIN");
                    return ResponseEntity.status(401).body(error);
                }

                Athlete target = athleteService.getAthleteById(id);
                if (target == null || target.getUserId() == null ||
                        !target.getUserId().equals(StpUtil.getLoginIdAsLong())) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "无权访问该运动员信息");
                    return ResponseEntity.status(403).body(error);
                }
            }

            AthleteProfileDTO profile = athleteService.getAthleteProfile(id);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("profile", profile);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取当前登录用户的运动员个人资料（包含历史记录和生涯统计）
     * 
     * @return 运动员个人资料
     */
    @Operation(summary = "获取当前用户完整个人资料", description = "获取当前登录用户的运动员详细资料，包含历史记录和生涯统计")
    @ApiResponse(responseCode = "200", description = "成功获取个人资料")
    // @SaCheckLogin  // 测试环境暂时注释
    @GetMapping("/my-profile")
    public ResponseEntity<?> getMyAthleteProfile() {
        try {
            // 检查用户是否已登录
            if (!StpUtil.isLogin()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "请先登录");
                error.put("code", "NOT_LOGIN");
                return ResponseEntity.status(401).body(error);
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            System.out.println(">>> [DEBUG] getMyAthleteProfile - StpUtil.getLoginIdAsLong() 返回: " + userId);
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            System.out.println(">>> [DEBUG] getMyAthleteProfile - 查询到的运动员: " + (athlete != null ? athlete.getId() + ", userId=" + athlete.getUserId() : "null"));
            
            if (athlete == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "运动员档案不存在");
                return ResponseEntity.badRequest().body(error);
            }
            
            AthleteProfileDTO profile = athleteService.getAthleteProfile(athlete.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("profile", profile);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 
