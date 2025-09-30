package com.aimlab.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
 * 射击记录控制器
 */
@Tag(name = "射击记录管理", description = "射击记录添加和查询相关接口")
@RestController
@RequestMapping("/api/records")
public class ShootingRecordController {

    @Autowired
    private CompetitionService competitionService;
    
    /**
     * 添加比赛射击记录
     * 
     * @param record 射击记录
     * @return 添加结果
     */
    @Operation(summary = "添加比赛射击记录", description = "为比赛添加新的射击记录")
    @ApiResponse(responseCode = "200", description = "射击记录添加成功")
    // @SaCheckLogin
    @PostMapping("/competition")
    public ResponseEntity<?> addCompetitionRecord(@RequestBody ShootingRecord record) {
        try {
            ShootingRecord savedRecord = competitionService.addCompetitionRecord(record);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "射击记录已添加");
            result.put("record", savedRecord);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取比赛的所有射击记录
     * 
     * @param competitionId 比赛ID
     * @return 射击记录列表
     */
    @Operation(summary = "获取比赛射击记录", description = "获取指定比赛的所有射击记录")
    @ApiResponse(responseCode = "200", description = "成功获取射击记录")
    @GetMapping("/competition/{competitionId}")
    public ResponseEntity<?> getCompetitionRecords(@Parameter(description = "比赛ID") @PathVariable Integer competitionId) {
        try {
            List<ShootingRecord> records = competitionService.getCompetitionRecords(competitionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("records", records);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取运动员在比赛中的射击记录
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     * @return 射击记录列表
     */
    @Operation(summary = "获取运动员比赛射击记录", description = "获取指定运动员在指定比赛中的所有射击记录")
    @ApiResponse(responseCode = "200", description = "成功获取运动员射击记录")
    @GetMapping("/competition/{competitionId}/athlete/{athleteId}")
    public ResponseEntity<?> getAthleteCompetitionRecords(
            @Parameter(description = "比赛ID") @PathVariable Integer competitionId, 
            @Parameter(description = "运动员ID") @PathVariable Long athleteId) {
        try {
            List<ShootingRecord> records = competitionService.getAthleteCompetitionRecords(competitionId, athleteId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("records", records);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 