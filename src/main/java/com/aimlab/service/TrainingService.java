package com.aimlab.service;

import com.aimlab.dto.TrainingReportDTO;
import com.aimlab.entity.Athlete;
import com.aimlab.entity.ShootingRecord;
import com.aimlab.entity.TrainingSession;
import com.aimlab.mapper.AthleteMapper;
import com.aimlab.mapper.ShootingRecordMapper;
import com.aimlab.mapper.TrainingSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 训练服务类
 */
@Service
public class TrainingService {

    @Autowired
    private TrainingSessionMapper trainingSessionMapper;
    
    @Autowired
    private AthleteMapper athleteMapper;
    
    @Autowired
    private ShootingRecordMapper shootingRecordMapper;
    
     @Autowired
    private PdfGenerationService pdfGenerationService;
    
    /**
     * 开始新的训练场次
     * 
     * @param athleteId 运动员ID
     * @param sessionName 训练场次名称
     * @return 创建的训练场次对象
     */
    @Transactional
    public TrainingSession startNewSession(long athleteId, String sessionName) {
        // 检查运动员是否存在
        Athlete athlete = athleteMapper.findById(athleteId);
        if (athlete == null) {
            throw new RuntimeException("运动员不存在");
        }
        
        // 创建新的训练场次
        TrainingSession session = new TrainingSession();
        session.setAthleteId(athleteId);
        session.setSessionName(sessionName);
        session.setStartTime(LocalDateTime.now());
        
        // 保存训练场次
        trainingSessionMapper.insert(session);
        
        return session;
    }
    
    /**
     * 结束训练场次
     * 
     * @param sessionId 训练场次ID
     * @param notes 训练备注
     * @return 更新后的训练场次对象
     */
    @Transactional
    public TrainingSession endSession(long sessionId, String notes) {
        // 检查训练场次是否存在
        TrainingSession session = trainingSessionMapper.findById(sessionId);
        if (session == null) {
            throw new RuntimeException("训练场次不存在");
        }
        
        // 设置结束时间和备注
        LocalDateTime endTime = LocalDateTime.now();
        session.setEndTime(endTime);
        session.setNotes(notes);
        
        // 更新训练场次
        trainingSessionMapper.update(session);
        
        return session;
    }
    
    /**
     * 获取运动员的训练场次列表
     * 
     * @param athleteId 运动员ID
     * @return 训练场次列表
     */
    public List<TrainingSession> getSessionsByAthleteId(long athleteId) {
        return trainingSessionMapper.findByAthleteId(athleteId);
    }
    
    /**
     * 获取训练场次详情
     * 
     * @param sessionId 训练场次ID
     * @return 训练场次对象
     */
    public TrainingSession getSessionById(long sessionId) {
        return trainingSessionMapper.findById(sessionId);
    }
    
    /**
     * 添加训练射击记录
     * 
     * @param record 射击记录对象
     * @return 添加的记录对象
     */
    @Transactional
    public ShootingRecord addTrainingRecord(ShootingRecord record) {
        // 检查训练场次是否存在
        Long trainingSessionId = record.getTrainingSessionId();
        if (trainingSessionId == null) {
            throw new RuntimeException("训练场次ID不能为空");
        }
        
        TrainingSession session = trainingSessionMapper.findById(trainingSessionId);
        if (session == null) {
            throw new RuntimeException("训练场次不存在");
        }
        
        // 检查训练场次是否已结束
        if (session.getEndTime() != null) {
            throw new RuntimeException("训练场次已结束，不能添加记录");
        }
        
        // 设置记录类型和射击时间
        record.setRecordType("TRAINING");
        record.setShotAt(LocalDateTime.now());
        
        // 设置用户ID（用于分片）
        record.setUserId(session.getAthleteId());
        
        // 保存射击记录
        shootingRecordMapper.insert(record);
        
        return record;
    }
    
    /**
     * 获取训练场次的射击记录
     * 
     * @param sessionId 训练场次ID
     * @return 射击记录列表
     */
    public List<ShootingRecord> getTrainingRecords(long sessionId) {
        return shootingRecordMapper.findByTrainingSessionId(sessionId);
    }
    
    /**
     * 生成训练报告
     * 
     * @param trainingSessionId 训练场次ID
     * @return 训练报告DTO
     */
    public TrainingReportDTO getTrainingReport(long trainingSessionId) {
        // 获取训练场次信息
        TrainingSession session = trainingSessionMapper.findById(trainingSessionId);
        if (session == null) {
            throw new RuntimeException("训练场次不存在");
        }
        
        // 获取该训练场次的所有射击记录
        List<ShootingRecord> records = shootingRecordMapper.findByTrainingSessionId(trainingSessionId);
        
        // 从训练场次构建基本报告
        TrainingReportDTO report = TrainingReportDTO.fromTrainingSession(session);
        
        // 如果没有射击记录，返回基本信息
        if (records == null || records.isEmpty()) {
            report.setTotalShots(0);
            report.setAverageScore(BigDecimal.ZERO);
            report.setBestScore(BigDecimal.ZERO);
            report.setWorstScore(BigDecimal.ZERO);
            report.setStabilityIndex(BigDecimal.ZERO);
            report.setShotsPerMinute(BigDecimal.ZERO);
            report.setScoreDistribution(new HashMap<>());
            report.setScoreTimeline(new ArrayList<>());
            return report;
        }
        
        // 计算统计数据
        int totalShots = records.size();
        report.setTotalShots(totalShots);
        
        // 计算平均环数
        BigDecimal totalScore = records.stream()
                .map(ShootingRecord::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageScore = totalScore.divide(new BigDecimal(totalShots), 2, RoundingMode.HALF_UP);
        report.setAverageScore(averageScore);
        
        // 计算最高环数
        BigDecimal bestScore = records.stream()
                .map(ShootingRecord::getScore)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        report.setBestScore(bestScore);
        
        // 计算最低环数
        BigDecimal worstScore = records.stream()
                .map(ShootingRecord::getScore)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        report.setWorstScore(worstScore);
        
        // 计算稳定性指数（环数的标准差）
        BigDecimal sumOfSquaredDifferences = records.stream()
                .map(record -> {
                    BigDecimal diff = record.getScore().subtract(averageScore);
                    return diff.multiply(diff);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variance = sumOfSquaredDifferences.divide(new BigDecimal(totalShots), 4, RoundingMode.HALF_UP);
        BigDecimal stabilityIndex = new BigDecimal(Math.sqrt(variance.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);
        report.setStabilityIndex(stabilityIndex);
        
        // 计算射击频率（每分钟射击次数）
        if (report.getDurationMinutes() != null && report.getDurationMinutes() > 0) {
            BigDecimal shotsPerMinute = new BigDecimal(totalShots)
                    .divide(new BigDecimal(report.getDurationMinutes()), 2, RoundingMode.HALF_UP);
            report.setShotsPerMinute(shotsPerMinute);
        } else {
            report.setShotsPerMinute(BigDecimal.ZERO);
        }
        
        // 计算各环数的分布统计
        Map<Integer, Integer> scoreDistribution = new HashMap<>();
        records.forEach(record -> {
            int scoreInt = record.getScore().intValue();
            scoreDistribution.put(scoreInt, scoreDistribution.getOrDefault(scoreInt, 0) + 1);
        });
        report.setScoreDistribution(scoreDistribution);
        
        // 按时间顺序的环数变化趋势
        List<BigDecimal> scoreTimeline = records.stream()
                .sorted((r1, r2) -> r1.getShotAt().compareTo(r2.getShotAt()))
                .map(ShootingRecord::getScore)
                .collect(Collectors.toList());
        report.setScoreTimeline(scoreTimeline);
        
        return report;
    }
    
    /**
     * 生成训练报告PDF
     * 
     * @param trainingSessionId 训练场次ID
     * @return PDF文件的字节数组
     */
    public byte[] getTrainingReportAsPdf(long trainingSessionId) {
        // 首先获取训练报告数据
        TrainingReportDTO reportDTO = getTrainingReport(trainingSessionId);
        
        // 然后生成PDF
        return pdfGenerationService.generateTrainingReportPdf(reportDTO);
    }
    
    /**
     * 更新训练场次信息
     * 
     * @param session 训练场次对象
     * @return 更新后的训练场次对象
     */
    @Transactional
    public TrainingSession updateSession(TrainingSession session) {
        trainingSessionMapper.update(session);
        return session;
    }
} 