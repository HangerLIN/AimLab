package com.aimlab.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.dto.AthleteProfileDTO;
import com.aimlab.entity.Athlete;
import com.aimlab.service.AthleteService;
import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
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
     * 获取所有运动员列表（公开接口）
     * 
     * @return 运动员列表
     */
    @Operation(summary = "获取所有运动员列表", description = "获取系统中所有已批准的运动员的基本信息")
    @ApiResponse(responseCode = "200", description = "成功获取运动员列表")
    @GetMapping
    public ResponseEntity<?> getAllAthletes() {
        try {
            java.util.List<Athlete> allAthletes = athleteService.getAllAthletes();
            
            // 过滤只返回已批准的运动员
            java.util.List<Athlete> approvedAthletes = allAthletes.stream()
                    .filter(a -> a.getApprovalStatus() != null && "APPROVED".equals(a.getApprovalStatus()))
                    .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("athletes", approvedAthletes);
            result.put("total", approvedAthletes.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 创建运动员档案
     * 
     * @param athlete 运动员信息
     * @return 创建结果
     */
    @Operation(summary = "创建运动员档案", description = "创建新的运动员档案信息")
    @ApiResponse(responseCode = "200", description = "运动员档案创建成功")
    @SaCheckLogin
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
    @SaCheckLogin
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
    @SaCheckLogin
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
    @SaCheckLogin
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
    @SaCheckLogin
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
    @SaCheckLogin
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
    
    /**
     * 上传运动员头像（存储到数据库BLOB）
     * 
     * @param file 头像文件
     * @return 上传结果
     */
    @Operation(summary = "上传运动员头像", description = "上传当前登录用户的运动员头像（存储到数据库）")
    @ApiResponse(responseCode = "200", description = "头像上传成功")
    @SaCheckLogin
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (!StpUtil.isLogin()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "请先登录");
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
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "只支持上传图片文件");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 验证文件大小 (最大16MB，MEDIUMBLOB限制)
            if (file.getSize() > 16 * 1024 * 1024) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "图片大小不能超过16MB");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 读取文件二进制数据并存储到数据库
            byte[] avatarData = file.getBytes();
            athleteService.updateAvatar(athlete.getId(), avatarData, contentType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "头像上传成功");
            result.put("avatarUrl", "/api/athletes/" + athlete.getId() + "/avatar");
            
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "文件读取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取运动员头像（从数据库BLOB读取）
     * 
     * @param id 运动员ID
     * @return 头像图片二进制数据
     */
    @Operation(summary = "获取运动员头像", description = "获取指定运动员的头像图片")
    @ApiResponse(responseCode = "200", description = "成功获取头像")
    @GetMapping("/{id}/avatar")
    public ResponseEntity<?> getAvatar(@Parameter(description = "运动员ID") @PathVariable Long id) {
        try {
            Athlete athlete = athleteService.getAthleteById(id);
            
            if (athlete == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] avatarData = athlete.getAvatarData();
            String avatarType = athlete.getAvatarType();
            
            if (avatarData == null || avatarData.length == 0) {
                // 返回默认头像或404
                return ResponseEntity.notFound().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(avatarType != null ? avatarType : "image/jpeg"));
            headers.setContentLength(avatarData.length);
            headers.setCacheControl("max-age=86400"); // 缓存1天
            
            return new ResponseEntity<>(avatarData, headers, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取当前用户的头像
     * 
     * @return 头像图片二进制数据
     */
    @Operation(summary = "获取当前用户头像", description = "获取当前登录用户的运动员头像")
    @ApiResponse(responseCode = "200", description = "成功获取头像")
    @SaCheckLogin
    @GetMapping("/my-avatar")
    public ResponseEntity<?> getMyAvatar() {
        try {
            if (!StpUtil.isLogin()) {
                return ResponseEntity.status(401).build();
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            
            if (athlete == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] avatarData = athlete.getAvatarData();
            String avatarType = athlete.getAvatarType();
            
            if (avatarData == null || avatarData.length == 0) {
                return ResponseEntity.notFound().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(avatarType != null ? avatarType : "image/jpeg"));
            headers.setContentLength(avatarData.length);
            
            return new ResponseEntity<>(avatarData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取等级枚举列表
     * 
     * @return 等级列表
     */
    @Operation(summary = "获取等级枚举列表", description = "获取运动员可选的等级列表")
    @ApiResponse(responseCode = "200", description = "成功获取等级列表")
    @GetMapping("/levels")
    public ResponseEntity<?> getLevelOptions() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("levels", java.util.Arrays.asList("国家级", "省级", "市级", "业余"));
        return ResponseEntity.ok(result);
    }
} 
