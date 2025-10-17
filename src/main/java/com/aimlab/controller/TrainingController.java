package com.aimlab.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.dto.TrainingReportDTO;
import com.aimlab.entity.Athlete;
import com.aimlab.entity.ShootingRecord;
import com.aimlab.entity.TrainingSession;
import com.aimlab.service.AthleteService;
import com.aimlab.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 训练控制器
 */
@Tag(name = "训练管理", description = "训练场次、射击记录和报告相关接口")
@RestController
@RequestMapping("/api/training")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;
    
    @Autowired
    private AthleteService athleteService;
    
    /**
     * 开始新的训练场次
     * 
     * @param params 请求参数，包含sessionName
     * @return 创建结果
     */
    @Operation(summary = "开始新的训练场次", description = "创建一个新的训练场次，需要提供训练名称")
    @ApiResponse(responseCode = "200", description = "训练场次创建成功")
    @SaCheckLogin
    @PostMapping("/sessions/start")
    public ResponseEntity<?> startTrainingSession(@RequestBody Map<String, String> params) {
        try {
            // 获取当前登录用户
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取当前用户的运动员信息
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            if (athlete == null) {
                throw new RuntimeException("请先创建运动员档案");
            }
            
            // 获取训练场次名称
            String sessionName = params.getOrDefault("name", "训练场次 " + System.currentTimeMillis());
            
            // 开始新的训练场次
            TrainingSession session = trainingService.startNewSession(athlete.getId(), sessionName);
            
            // 如果有备注，设置备注
            if (params.containsKey("description")) {
                session.setNotes(params.get("description"));
                trainingService.updateSession(session);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("session", session);
            result.put("message", "训练场次已开始");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 结束训练场次
     * 
     * @param sessionId 训练场次ID
     * @param params 请求参数，包含notes
     * @return 结束结果
     */
    @Operation(summary = "结束训练场次", description = "结束指定的训练场次，可以添加训练备注")
    @ApiResponse(responseCode = "200", description = "训练场次结束成功")
    @SaCheckLogin
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<?> endTrainingSession(
            @Parameter(description = "训练场次ID") @PathVariable Long sessionId, 
            @RequestBody Map<String, String> params) {
        try {
            // 获取当前登录用户
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取当前用户的运动员信息
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            if (athlete == null) {
                throw new RuntimeException("请先创建运动员档案");
            }
            
            // 获取训练场次
            TrainingSession session = trainingService.getSessionById(sessionId);
            if (session == null) {
                throw new RuntimeException("训练场次不存在");
            }
            
            // 检查是否是当前运动员的训练场次
            if (!session.getAthleteId().equals(athlete.getId())) {
                throw new RuntimeException("无权结束此训练场次");
            }
            
            // 设置训练备注
            if (params.containsKey("notes")) {
                session.setNotes(params.get("notes"));
            }
            
            // 结束训练场次
            session = trainingService.endSession(sessionId, session.getNotes());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("session", session);
            result.put("message", "训练场次 已结束");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取当前用户的训练场次列表
     * 
     * @return 训练场次列表
     */
    @SaCheckLogin
    @GetMapping("/sessions")
    public ResponseEntity<?> getSessionList() {
        try {
            // 获取当前登录用户
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取当前用户的运动员信息
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            if (athlete == null) {
                throw new RuntimeException("请先创建运动员档案");
            }
            
            // 获取训练场次列表
            List<TrainingSession> sessions = trainingService.getSessionsByAthleteId(athlete.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("sessions", sessions);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取训练场次详情
     * 
     * @param sessionId 训练场次ID
     * @return 训练场次详情
     */
    @SaCheckLogin
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<?> getSessionDetail(@PathVariable Long sessionId) {
        try {
            // 获取当前登录用户
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取当前用户的运动员信息
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            if (athlete == null) {
                throw new RuntimeException("请先创建运动员档案");
            }
            
            // 获取训练场次
            TrainingSession session = trainingService.getSessionById(sessionId);
            if (session == null) {
                throw new RuntimeException("训练场次不存在");
            }
            
            // 检查是否是当前运动员的训练场次
            if (!session.getAthleteId().equals(athlete.getId())) {
                throw new RuntimeException("无权查看此训练场次");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("session", session);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 添加训练射击记录
     * 
     * @param record 射击记录
     * @return 添加结果
     */
    @SaCheckLogin
    @PostMapping("/records")
    public ResponseEntity<?> addTrainingRecord(@RequestBody ShootingRecord record) {
        try {
            // 获取当前登录用户
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取当前用户的运动员信息
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            if (athlete == null) {
                throw new RuntimeException("请先创建运动员档案");
            }
            
            // 设置运动员ID
            record.setAthleteId(athlete.getId());
            
            // 添加训练射击记录
            ShootingRecord savedRecord = trainingService.addTrainingRecord(record);
            
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
     * 获取训练场次的射击记录
     * 
     * @param sessionId 训练场次ID
     * @return 射击记录列表
     */
    @SaCheckLogin
    @GetMapping("/sessions/{sessionId}/records")
    public ResponseEntity<?> getTrainingRecords(@PathVariable Long sessionId) {
        try {
            // 获取当前登录用户
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取当前用户的运动员信息
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            if (athlete == null) {
                throw new RuntimeException("请先创建运动员档案");
            }
            
            // 获取训练场次
            TrainingSession session = trainingService.getSessionById(sessionId);
            if (session == null) {
                throw new RuntimeException("训练场次不存在");
            }
            
            // 检查是否是当前运动员的训练场次
            if (!session.getAthleteId().equals(athlete.getId())) {
                throw new RuntimeException("无权查看此训练场次的记录");
            }
            
            // 获取训练射击记录
            List<ShootingRecord> records = trainingService.getTrainingRecords(sessionId);
            
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
     * 获取训练报告
     * 
     * @param sessionId 训练场次ID
     * @return 训练报告
     */
    @Operation(summary = "获取训练报告", description = "获取指定训练场次的详细报告，包含统计数据")
    @ApiResponse(responseCode = "200", description = "成功获取训练报告")
    @SaCheckLogin
    @GetMapping("/sessions/{sessionId}/report")
    public ResponseEntity<?> getTrainingReport(@Parameter(description = "训练场次ID") @PathVariable Long sessionId) {
        try {
            // 获取当前登录用户
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取当前用户的运动员信息
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            if (athlete == null) {
                throw new RuntimeException("请先创建运动员档案");
            }
            
            // 获取训练场次
            TrainingSession session = trainingService.getSessionById(sessionId);
            if (session == null) {
                throw new RuntimeException("训练场次不存在");
            }
            
            // 检查是否是当前运动员的训练场次
            if (!session.getAthleteId().equals(athlete.getId())) {
                throw new RuntimeException("无权查看此训练报告");
            }
            
            // 获取训练报告
            TrainingReportDTO report = trainingService.getTrainingReport(sessionId);
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 下载训练报告PDF
     * 
     * @param sessionId 训练场次ID
     * @return PDF文件
     */
    @Operation(summary = "下载训练报告PDF", description = "获取指定训练场次的PDF格式报告")
    @ApiResponse(responseCode = "200", description = "成功获取PDF报告", content = @Content(mediaType = "application/pdf"))
    @SaCheckLogin
    @GetMapping("/sessions/{sessionId}/report/pdf")
    public ResponseEntity<byte[]> downloadTrainingReportPdf(@Parameter(description = "训练场次ID") @PathVariable Long sessionId) {
        try {
            // 获取当前登录用户
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取当前用户的运动员信息
            Athlete athlete = athleteService.getAthleteByUserId(userId);
            if (athlete == null) {
                throw new RuntimeException("请先创建运动员档案");
            }
            
            // 获取训练场次
            TrainingSession session = trainingService.getSessionById(sessionId);
            if (session == null) {
                throw new RuntimeException("训练场次不存在");
            }
            
            // 检查是否是当前运动员的训练场次
            if (!session.getAthleteId().equals(athlete.getId())) {
                throw new RuntimeException("无权查看此训练报告");
            }
            
            // 生成PDF
            byte[] pdfBytes = trainingService.getTrainingReportAsPdf(sessionId);
            
            // 设置HTTP响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Training-Report-" + sessionId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            // 由于这是文件下载接口，无法返回JSON错误信息，只能抛出异常
            throw new RuntimeException("下载PDF失败: " + e.getMessage(), e);
        }
    }
} 
