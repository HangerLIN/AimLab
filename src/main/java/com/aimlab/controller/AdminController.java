package com.aimlab.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.aimlab.dto.AdminUserDTO;
import com.aimlab.dto.ExportFile;
import com.aimlab.dto.ImportResult;
import com.aimlab.dto.PageResult;
import com.aimlab.entity.Competition;
import com.aimlab.entity.Athlete;
import com.aimlab.service.AdminService;
import com.aimlab.service.AdminUserService;
import com.aimlab.service.AthleteImportExportService;
import com.aimlab.service.AthleteService;
import com.aimlab.service.CompetitionService;
import com.aimlab.service.TrainingAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员接口控制器
 */
@Tag(name = "管理员中心", description = "系统级管理能力接口")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AthleteImportExportService athleteImportExportService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private TrainingAnalyticsService trainingAnalyticsService;

    /**
     * 获取仪表盘统计数据
     *
     * @return 统计数据
     */
    @Operation(summary = "仪表盘统计", description = "获取系统全局统计信息")
    @SaCheckPermission("admin:dashboard")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardMetrics() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("metrics", adminService.getDashboardMetrics());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取全部用户（脱敏）
     *
     * @return 用户列表
     */
    @Operation(summary = "用户列表", description = "按条件筛选系统用户并展示关联档案信息")
    @SaCheckPermission("admin:users")
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        PageResult<AdminUserDTO> pageResult = adminUserService.listUsers(username, role, status, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("total", pageResult.getTotal());
        result.put("items", pageResult.getItems());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "创建系统用户", description = "管理员创建新用户账号")
    @SaCheckPermission("admin:users:manage")
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");
        String role = (String) payload.get("role");
        Integer status = payload.get("status") != null ? ((Number) payload.get("status")).intValue() : 1;

        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        Long userId = adminUserService.createUser(username.trim(), password.trim(), role, status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "更新用户信息", description = "调整用户角色或状态")
    @SaCheckPermission("admin:users:manage")
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> payload) {
        if (payload.containsKey("status")) {
            Integer status = payload.get("status") != null ? ((Number) payload.get("status")).intValue() : null;
            adminUserService.updateStatus(userId, status);
        }
        if (payload.containsKey("role")) {
            String role = (String) payload.get("role");
            adminUserService.updateRole(userId, role);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "重置用户密码", description = "管理员为指定用户重置密码")
    @SaCheckPermission("admin:users:manage")
    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> payload) {
        String newPassword = (String) payload.get("password");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        adminUserService.resetPassword(userId, newPassword.trim());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "密码已重置并强制用户下线");
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "强制踢出用户", description = "管理员强制下线指定用户")
    @SaCheckPermission("admin:users:manage")
    @PostMapping("/users/{userId}/kick")
    public ResponseEntity<?> kickUser(@PathVariable Long userId) {
        adminUserService.forceLogout(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "用户已被强制下线");
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "删除用户", description = "管理员删除用户账号")
    @SaCheckPermission("admin:users:manage")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "用户已删除");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取全部运动员
     *
     * @return 运动员列表
     */
    @Operation(summary = "运动员列表", description = "获取全部运动员基础档案数据")
    @SaCheckPermission("admin:athletes")
    @GetMapping("/athletes")
    public ResponseEntity<?> getAllAthletes() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("athletes", adminService.listAthletes());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "导出运动员档案", description = "支持导出为CSV或Excel格式")
    @SaCheckPermission("admin:athletes:export")
    @GetMapping("/athletes/export")
    public ResponseEntity<byte[]> exportAthletes(@RequestParam(value = "format", defaultValue = "csv") String format) {
        ExportFile exportFile = athleteImportExportService.exportAthletes(format);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + exportFile.getFileName());
        headers.setContentType(MediaType.parseMediaType(exportFile.getContentType()));
        return ResponseEntity.ok().headers(headers).body(exportFile.getData());
    }

    @Operation(summary = "导入运动员档案", description = "支持上传CSV/Excel文件批量导入")
    @SaCheckPermission("admin:athletes:import")
    @PostMapping(value = "/athletes/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importAthletes(@RequestParam("file") MultipartFile file) {
        ImportResult importResult = athleteImportExportService.importAthletes(file);
        Map<String, Object> result = new HashMap<>();
        result.put("success", !importResult.hasErrors());
        result.put("imported", importResult.getSuccessCount());
        result.put("importedCount", importResult.getSuccessCount());
        result.put("errors", importResult.getErrors());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "更新运动员信息", description = "管理员编辑运动员档案")
    @SaCheckPermission("admin:athletes")
    @PutMapping("/athletes/{athleteId}")
    public ResponseEntity<?> updateAthlete(@PathVariable Long athleteId, @RequestBody Athlete athlete) {
        try {
            athlete.setId(athleteId);
            boolean updated = athleteService.updateAthleteProfile(athlete);
            Map<String, Object> result = new HashMap<>();
            result.put("success", updated);
            result.put("message", updated ? "更新成功" : "更新失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "审批运动员", description = "管理员审批通过运动员档案")
    @SaCheckPermission("admin:athletes")
    @PutMapping("/athletes/{athleteId}/approve")
    public ResponseEntity<?> approveAthlete(@PathVariable Long athleteId) {
        try {
            athleteService.updateApprovalStatus(athleteId, "APPROVED");
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "审批通过");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "拒绝运动员", description = "管理员拒绝运动员档案")
    @SaCheckPermission("admin:athletes")
    @PutMapping("/athletes/{athleteId}/reject")
    public ResponseEntity<?> rejectAthlete(@PathVariable Long athleteId) {
        try {
            athleteService.updateApprovalStatus(athleteId, "REJECTED");
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "已拒绝");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "审批档案修改", description = "管理员审批通过运动员档案修改")
    @SaCheckPermission("admin:athletes")
    @PutMapping("/athletes/{athleteId}/approve-modification")
    public ResponseEntity<?> approveAthleteModification(@PathVariable Long athleteId, @RequestBody(required = false) Athlete modificationData) {
        try {
            Athlete athlete = athleteService.getAthleteById(athleteId);
            if (athlete == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "运动员不存在");
                return ResponseEntity.badRequest().body(error);
            }

            if (!"PENDING".equals(athlete.getModificationStatus())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "该运动员档案无待审批的修改");
                return ResponseEntity.badRequest().body(error);
            }

            // 如果没有提供修改数据，则不应用任何修改，仅清除待审批状态
            if (modificationData == null) {
                modificationData = new Athlete();
            }
            
            boolean success = athleteService.approveModification(athleteId, modificationData);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", "档案修改已审批通过");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "拒绝档案修改", description = "管理员拒绝运动员档案修改")
    @SaCheckPermission("admin:athletes")
    @PutMapping("/athletes/{athleteId}/reject-modification")
    public ResponseEntity<?> rejectAthleteModification(@PathVariable Long athleteId) {
        try {
            Athlete athlete = athleteService.getAthleteById(athleteId);
            if (athlete == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "运动员不存在");
                return ResponseEntity.badRequest().body(error);
            }

            if (!"PENDING".equals(athlete.getModificationStatus())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "该运动员档案无待审批的修改");
                return ResponseEntity.badRequest().body(error);
            }

            boolean success = athleteService.rejectModification(athleteId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", "档案修改已拒绝");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "获取待审批修改列表", description = "管理员查看所有档案修改待审批的运动员")
    @SaCheckPermission("admin:athletes")
    @GetMapping("/athletes/pending-modifications")
    public ResponseEntity<?> getPendingModifications(
            @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            List<Athlete> athletes = athleteService.getAllAthletes();
            // 过滤出修改状态为 PENDING 的运动员
            List<Athlete> pendingModifications = athletes.stream()
                    .filter(a -> "PENDING".equals(a.getModificationStatus()))
                    .collect(java.util.stream.Collectors.toList());

            if (limit != null && limit > 0) {
                pendingModifications = pendingModifications.stream()
                        .limit(limit)
                        .collect(java.util.stream.Collectors.toList());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("athletes", pendingModifications);
            result.put("total", pendingModifications.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "获取待审批运动员列表", description = "管理员查看所有待审批的运动员档案")
    @SaCheckPermission("admin:athletes")
    @GetMapping("/athletes/pending")
    public ResponseEntity<?> getPendingAthletes(
            @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            List<Athlete> pendingAthletes = athleteService.getPendingAthletes(limit);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("athletes", pendingAthletes);
            result.put("total", pendingAthletes.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "获取已批准运动员列表", description = "管理员查看所有已批准的运动员档案")
    @SaCheckPermission("admin:athletes")
    @GetMapping("/athletes/approved")
    public ResponseEntity<?> getApprovedAthletes(
            @RequestParam(value = "limit", required = false) Integer limit) {
        try {
            List<Athlete> approvedAthletes = athleteService.getApprovedAthletes(limit);
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

    @Operation(summary = "删除运动员", description = "管理员删除运动员档案")
    @SaCheckPermission("admin:athletes")
    @DeleteMapping("/athletes/{athleteId}")
    public ResponseEntity<?> deleteAthlete(@PathVariable Long athleteId) {
        try {
            athleteService.deleteAthlete(athleteId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "创建比赛", description = "管理员创建新的比赛并配置赛制、报名窗口")
    @SaCheckPermission("admin:competitions.manage")
    @PostMapping("/competitions")
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition) {
        try {
            Competition created = competitionService.createCompetition(competition);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("competition", created);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "更新比赛配置", description = "管理员编辑比赛的赛制、报名窗口和权限")
    @SaCheckPermission("admin:competitions.manage")
    @PutMapping("/competitions/{competitionId}")
    public ResponseEntity<?> updateCompetition(@PathVariable Integer competitionId,
                                               @RequestBody Competition competition) {
        try {
            competition.setId(competitionId);
            Competition updated = competitionService.updateCompetition(competition);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("competition", updated);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(summary = "训练统计 - 时间维度", description = "按时间周期聚合训练成绩与稳定性")
    @SaCheckPermission("admin:training.analytics")
    @GetMapping("/training/analytics/time")
    public ResponseEntity<?> getTrainingAnalyticsByTime(
            @RequestParam(value = "granularity", defaultValue = "DAY") String granularity,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "athleteId", required = false) Long athleteId,
            @RequestParam(value = "projectType", required = false) String projectType) {
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("stats", trainingAnalyticsService.getTimeStats(granularity, start, end, athleteId, projectType));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "训练统计 - 运动员维度", description = "按运动员汇总训练表现")
    @SaCheckPermission("admin:training.analytics")
    @GetMapping("/training/analytics/athlete")
    public ResponseEntity<?> getTrainingAnalyticsByAthlete(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "projectType", required = false) String projectType,
            @RequestParam(value = "keyword", required = false) String keyword) {
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("stats", trainingAnalyticsService.getAthleteStats(start, end, projectType, keyword));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "训练统计 - 项目维度", description = "按训练项目聚合训练表现")
    @SaCheckPermission("admin:training.analytics")
    @GetMapping("/training/analytics/project")
    public ResponseEntity<?> getTrainingAnalyticsByProject(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "athleteId", required = false) Long athleteId) {
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("stats", trainingAnalyticsService.getProjectStats(start, end, athleteId));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "导出训练统计报表", description = "按指定维度导出训练表现报表")
    @SaCheckPermission("admin:training.export")
    @GetMapping("/training/analytics/export")
    public ResponseEntity<byte[]> exportTrainingAnalytics(
            @RequestParam(value = "dimension", defaultValue = "TIME") String dimension,
            @RequestParam(value = "granularity", defaultValue = "DAY") String granularity,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "athleteId", required = false) Long athleteId,
            @RequestParam(value = "projectType", required = false) String projectType,
            @RequestParam(value = "keyword", required = false) String keyword) {
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        ExportFile exportFile = trainingAnalyticsService.exportAnalytics(dimension, granularity, start, end, athleteId, projectType, keyword);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + exportFile.getFileName());
        headers.setContentType(MediaType.parseMediaType(exportFile.getContentType()));
        return ResponseEntity.ok().headers(headers).body(exportFile.getData());
    }

    @Operation(summary = "导出比赛成绩", description = "导出已完成比赛的成绩单")
    @SaCheckPermission("admin:competitions.export")
    @GetMapping("/competitions/{competitionId}/results/export")
    public ResponseEntity<byte[]> exportCompetitionResults(@PathVariable Integer competitionId,
                                                           @RequestParam(value = "format", defaultValue = "xlsx") String format) {
        ExportFile exportFile = competitionService.exportCompetitionResults(competitionId, format);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + exportFile.getFileName());
        headers.setContentType(MediaType.parseMediaType(exportFile.getContentType()));
        return ResponseEntity.ok().headers(headers).body(exportFile.getData());
    }

    /**
     * 强制结束比赛
     *
     * @param competitionId 比赛ID
     * @param payload       请求体，包含强制结束原因
     * @return 更新后的比赛信息
     */
    @Operation(summary = "强制结束比赛", description = "管理员强制结束指定比赛")
    @SaCheckPermission("competition:force-finish")
    @PostMapping("/competitions/{competitionId}/force-finish")
    public ResponseEntity<?> forceFinishCompetition(
            @PathVariable Integer competitionId,
            @RequestBody(required = false) Map<String, String> payload) {
        String reason = payload != null ? payload.getOrDefault("reason", "") : "";
        Competition competition = competitionService.forceFinishCompetition(competitionId, reason);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "比赛已强制结束");
        result.put("competition", competition);
        return ResponseEntity.ok(result);
    }

    /**
     * 下载比赛结果报告
     *
     * @param competitionId 比赛ID
     * @return PDF文件
     */
    @Operation(summary = "导出比赛统计报表", description = "下载比赛结果统计报表（PDF）")
    @SaCheckPermission("admin:reports")
    @GetMapping("/competitions/{competitionId}/results/pdf")
    public ResponseEntity<byte[]> downloadCompetitionResultReport(@PathVariable Integer competitionId) {
        byte[] pdfBytes = competitionService.getCompetitionResultsAsPdf(competitionId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Competition-Results-" + competitionId + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("时间格式错误，请使用ISO格式，例如2024-01-01T00:00:00");
        }
    }
}
