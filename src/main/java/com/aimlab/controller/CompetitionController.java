package com.aimlab.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.dto.RankingItemDTO;
import com.aimlab.entity.Competition;
import com.aimlab.entity.CompetitionAthlete;
import com.aimlab.entity.CompetitionResult;
import com.aimlab.entity.CompetitionStatus;
import com.aimlab.entity.ShootingRecord;
import com.aimlab.service.CompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 比赛控制器
 */
@Tag(name = "比赛管理", description = "比赛创建、管理和结果查询相关接口")
@RestController
@RequestMapping("/api/competitions")
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;
    
    /**
     * 创建比赛
     * 
     * @param competition 比赛信息
     * @return 创建结果
     */
    @Operation(summary = "创建比赛", description = "创建新的比赛，需要ADMIN角色权限")
    @ApiResponse(responseCode = "200", description = "比赛创建成功")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PostMapping
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition) {
        try {
            Competition created = competitionService.createCompetition(competition);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "比赛创建成功");
            result.put("competition", created);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 运动员报名参赛
     * 
     * @param competitionId 比赛ID
     * @param params 请求参数，包含athleteIds
     * @return 报名结果
     */
    @Operation(summary = "运动员报名参赛", description = "为一个或多个运动员报名参加比赛")
    @ApiResponse(responseCode = "200", description = "报名成功")
    @SaCheckLogin
    @PostMapping("/{competitionId}/enroll")
    public ResponseEntity<?> enrollAthletes(
            @Parameter(description = "比赛ID") @PathVariable Integer competitionId,
            @RequestBody Map<String, List<Long>> params) {
        try {
            List<Long> athleteIds = params.get("athleteIds");
            if (athleteIds == null || athleteIds.isEmpty()) {
                throw new RuntimeException("运动员ID列表不能为空");
            }
            
            int enrolledCount = competitionService.enrollAthletes(competitionId, athleteIds);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "成功报名" + enrolledCount + "名运动员");
            result.put("enrolledCount", enrolledCount);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 开始比赛
     * 
     * @param competitionId 比赛ID
     * @return 开始结果
     */
    @Operation(summary = "开始比赛", description = "开始指定ID的比赛，需要ADMIN角色权限")
    @ApiResponse(responseCode = "200", description = "比赛已开始")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PostMapping("/{competitionId}/start")
    public ResponseEntity<?> startCompetition(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            Competition competition = competitionService.startCompetition(competitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "比赛已开始");
            result.put("competition", competition);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 暂停比赛
     * 
     * @param competitionId 比赛ID
     * @return 暂停结果
     */
    @Operation(summary = "暂停比赛", description = "暂停正在进行的比赛，需要ADMIN角色权限")
    @ApiResponse(responseCode = "200", description = "比赛已暂停")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PostMapping("/{competitionId}/pause")
    public ResponseEntity<?> pauseCompetition(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            Competition competition = competitionService.pauseCompetition(competitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "比赛已暂停");
            result.put("competition", competition);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 恢复比赛
     * 
     * @param competitionId 比赛ID
     * @return 恢复结果
     */
    @Operation(summary = "恢复比赛", description = "恢复已暂停的比赛，需要ADMIN角色权限")
    @ApiResponse(responseCode = "200", description = "比赛已恢复")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PostMapping("/{competitionId}/resume")
    public ResponseEntity<?> resumeCompetition(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            Competition competition = competitionService.resumeCompetition(competitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "比赛已恢复");
            result.put("competition", competition);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 完成比赛
     * 
     * @param competitionId 比赛ID
     * @return 完成结果
     */
    @Operation(summary = "完成比赛", description = "完成比赛并计算最终成绩，需要ADMIN角色权限")
    @ApiResponse(responseCode = "200", description = "比赛已完成")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PostMapping("/{competitionId}/complete")
    public ResponseEntity<?> completeCompetition(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            Competition competition = competitionService.completeCompetition(competitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "比赛已完成");
            result.put("competition", competition);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 取消比赛
     * 
     * @param competitionId 比赛ID
     * @return 取消结果
     */
    @Operation(summary = "取消比赛", description = "取消比赛，需要ADMIN角色权限")
    @ApiResponse(responseCode = "200", description = "比赛已取消")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PostMapping("/{competitionId}/cancel")
    public ResponseEntity<?> cancelCompetition(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            Competition competition = competitionService.cancelCompetition(competitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "比赛已取消");
            result.put("competition", competition);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取比赛实时状态
     * 
     * @param competitionId 比赛ID
     * @return 比赛实时状态
     */
    @Operation(summary = "获取比赛实时状态", description = "获取比赛的当前状态信息")
    @ApiResponse(responseCode = "200", description = "成功获取比赛状态")
    @GetMapping("/{competitionId}/status")
    public ResponseEntity<?> getCompetitionStatus(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            CompetitionStatus status = competitionService.getCompetitionStatus(competitionId);
            if (status == null) {
                throw new RuntimeException("比赛未开始或已结束");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("status", status);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取比赛列表
     * 
     * @param status 比赛状态（可选）
     * @return 比赛列表
     */
    @Operation(summary = "获取比赛列表", description = "获取所有比赛或按状态筛选比赛")
    @ApiResponse(responseCode = "200", description = "成功获取比赛列表")
    @GetMapping
    public ResponseEntity<?> getCompetitions(@Parameter(description = "比赛状态(可选)") @RequestParam(required = false) String status) {
        try {
            List<Competition> competitions;
            if (status != null && !status.isEmpty()) {
                competitions = competitionService.getCompetitionsByStatus(status);
            } else {
                competitions = competitionService.getAllCompetitions();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("competitions", competitions);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取我创建的比赛列表
     * 
     * @return 比赛列表
     */
    @Operation(summary = "获取我创建的比赛", description = "获取当前登录用户创建的所有比赛")
    @ApiResponse(responseCode = "200", description = "成功获取我创建的比赛")
    @SaCheckLogin
    @GetMapping("/my-created")
    public ResponseEntity<?> getMyCreatedCompetitions() {
        try {
            List<Competition> competitions = competitionService.getMyCreatedCompetitions();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("competitions", competitions);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取比赛详情
     * 
     * @param competitionId 比赛ID
     * @return 比赛详情
     */
    @Operation(summary = "获取比赛详情", description = "获取指定比赛的详细信息")
    @ApiResponse(responseCode = "200", description = "成功获取比赛详情")
    @GetMapping("/{competitionId}")
    public ResponseEntity<?> getCompetitionDetail(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            Competition competition = competitionService.getCompetitionById(competitionId);
            if (competition == null) {
                throw new RuntimeException("比赛不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("competition", competition);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取比赛参赛运动员列表
     * 
     * @param competitionId 比赛ID
     * @return 参赛运动员列表
     */
    @Operation(summary = "获取参赛运动员列表", description = "获取指定比赛的所有参赛运动员")
    @ApiResponse(responseCode = "200", description = "成功获取参赛运动员列表")
    @GetMapping("/{competitionId}/athletes")
    public ResponseEntity<?> getCompetitionAthletes(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            List<CompetitionAthlete> athletes = competitionService.getCompetitionAthletes(competitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("athletes", athletes);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取比赛实时排名
     * 
     * @param competitionId 比赛ID
     * @return 排名列表
     */
    @Operation(summary = "获取比赛实时排名", description = "获取正在进行中的比赛实时排名")
    @ApiResponse(responseCode = "200", description = "成功获取比赛排名")
    @GetMapping("/{competitionId}/rankings")
    public ResponseEntity<?> getLiveRanking(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            List<RankingItemDTO> rankings = competitionService.getLiveRanking(competitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("rankings", rankings);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取比赛最终结果
     * 
     * @param competitionId 比赛ID
     * @return 比赛结果列表
     */
    @Operation(summary = "获取比赛最终结果", description = "获取已完成比赛的最终成绩和排名")
    @ApiResponse(responseCode = "200", description = "成功获取比赛结果")
    @GetMapping("/{competitionId}/results")
    public ResponseEntity<?> getCompetitionResults(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            // 获取比赛信息
            Competition competition = competitionService.getCompetitionById(competitionId);
            if (competition == null) {
                throw new RuntimeException("比赛不存在");
            }
            
            // 检查比赛是否已结束
            if (!"COMPLETED".equals(competition.getStatus())) {
                throw new RuntimeException("比赛尚未结束，无法获取最终结果");
            }
            
            // 获取比赛结果
            List<CompetitionResult> results = competitionService.getCompetitionResults(competitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("results", results);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取运动员的比赛结果
     * 
     * @param athleteId 运动员ID
     * @return 比赛结果列表
     */
    @Operation(summary = "获取运动员比赛结果", description = "获取指定运动员的所有比赛成绩")
    @ApiResponse(responseCode = "200", description = "成功获取运动员比赛结果")
    @GetMapping("/athletes/{athleteId}/results")
    public ResponseEntity<?> getAthleteResults(@Parameter(description = "运动员ID") @PathVariable Long athleteId) {
        try {
            List<CompetitionResult> results = competitionService.getAthleteResults(athleteId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("results", results);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 