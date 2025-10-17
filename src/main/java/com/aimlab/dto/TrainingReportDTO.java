package com.aimlab.dto;

import com.aimlab.entity.TrainingSession;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 训练报告数据传输对象
 */
@Data
public class TrainingReportDTO {
    
    /**
     * 训练场次ID
     */
    private Long id;
    
    /**
     * 运动员ID
     */
    private Long athleteId;
    
    /**
     * 训练场次名称
     */
    private String sessionName;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 训练备注
     */
    private String notes;
    
    /**
     * 总射击次数
     */
    private Integer totalShots;
    
    /**
     * 平均环数
     */
    private BigDecimal averageScore;
    
    /**
     * 最高环数
     */
    private BigDecimal bestScore;
    
    /**
     * 最低环数
     */
    private BigDecimal worstScore;
    
    /**
     * 稳定性指数（环数的标准差，越低表示越稳定）
     */
    private BigDecimal stabilityIndex;
    
    /**
     * 训练时长（分钟）
     */
    private Long durationMinutes;
    
    /**
     * 射击频率（每分钟射击次数）
     */
    private BigDecimal shotsPerMinute;
    
    /**
     * 各环数的分布统计 {环数 -> 数量}
     */
    private Map<Integer, Integer> scoreDistribution;
    
    /**
     * 按时间顺序的环数变化趋势
     */
    private List<BigDecimal> scoreTimeline;

    /**
     * 射击记录列表
     */
    private List<ShootingRecordDTO> records;
    
    /**
     * 从TrainingSession构建基本信息
     * 
     * @param session 训练场次
     * @return TrainingReportDTO对象
     */
    public static TrainingReportDTO fromTrainingSession(TrainingSession session) {
        TrainingReportDTO report = new TrainingReportDTO();
        report.setId(session.getId());
        report.setAthleteId(session.getAthleteId());
        report.setSessionName(session.getSessionName());
        report.setStartTime(session.getStartTime());
        report.setEndTime(session.getEndTime());
        report.setNotes(session.getNotes());
        
        // 如果有开始和结束时间，计算训练时长
        if (session.getStartTime() != null && session.getEndTime() != null) {
            long durationMinutes = java.time.Duration.between(
                    session.getStartTime(), session.getEndTime()).toMinutes();
            report.setDurationMinutes(durationMinutes);
        }
        
        return report;
    }
} 
