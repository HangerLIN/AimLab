package com.aimlab.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.aimlab.dto.*;
import com.aimlab.service.AdvancedAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "数据分析", description = "运动员数据对比与趋势分析接口")
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    @Autowired
    private AdvancedAnalyticsService analyticsService;

    @Operation(summary = "运动员对比分析")
    @SaCheckLogin
    @PostMapping("/compare")
    public ResponseEntity<?> compareAthletes(@RequestBody CompareRequestDTO request) {
        log.info("收到运动员对比分析请求: {}", request);
        try {
            List<AthleteCompareDTO> result = analyticsService.compareAthletes(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("count", result.size());
            
            log.info("运动员对比分析成功, 返回 {} 条数据", result.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("运动员对比分析失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "快速对比分析")
    @SaCheckLogin
    @GetMapping("/compare")
    public ResponseEntity<?> quickCompare(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String athleteIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String projectType,
            @RequestParam(defaultValue = "5") Integer maxCount) {
        
        CompareRequestDTO request = new CompareRequestDTO();
        request.setLevel(level);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setProjectType(projectType);
        request.setMaxCount(maxCount);
        
        if (athleteIds != null && !athleteIds.trim().isEmpty()) {
            List<Long> ids = Arrays.stream(athleteIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
            request.setAthleteIds(ids);
        }
        
        return compareAthletes(request);
    }

    @Operation(summary = "趋势分析")
    @SaCheckLogin
    @GetMapping("/trend/{athleteId}")
    public ResponseEntity<?> getTrendAnalysis(
            @PathVariable Long athleteId,
            @RequestParam(defaultValue = "12") Integer weeks,
            @RequestParam(required = false) String projectType) {
        
        log.info("收到趋势分析请求: athleteId={}, weeks={}, projectType={}", athleteId, weeks, projectType);
        try {
            TrendAnalysisResultDTO result = analyticsService.getTrendAnalysis(athleteId, weeks, projectType);
            log.info("趋势分析成功: athleteId={}, 分析周数={}", athleteId, result.getAnalyzedWeeks());
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception e) {
            log.error("趋势分析失败: athleteId={}, error={}", athleteId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "导出对比数据")
    @SaCheckLogin
    @PostMapping("/compare/export")
    public ResponseEntity<?> exportCompareData(@RequestBody CompareRequestDTO request) {
        try {
            List<AthleteCompareDTO> data = analyticsService.compareAthletes(request);
            ExportFile file = analyticsService.exportCompareData(data, "xlsx");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + file.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(file.getData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "导出趋势数据")
    @SaCheckLogin
    @GetMapping("/trend/{athleteId}/export")
    public ResponseEntity<?> exportTrendData(
            @PathVariable Long athleteId,
            @RequestParam(defaultValue = "12") Integer weeks,
            @RequestParam(required = false) String projectType) {
        
        try {
            TrendAnalysisResultDTO data = analyticsService.getTrendAnalysis(athleteId, weeks, projectType);
            ExportFile file = analyticsService.exportTrendData(data);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + file.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(file.getData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "获取级别列表")
    @GetMapping("/levels")
    public ResponseEntity<?> getAvailableLevels() {
        List<String> levels = Arrays.asList("国家级", "省级", "市级", "业余");
        return ResponseEntity.ok(Map.of("success", true, "levels", levels));
    }
}
