package com.aimlab.service;

import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.dto.RankingItemDTO;
import com.aimlab.entity.Competition;
import com.aimlab.entity.CompetitionAthlete;
import com.aimlab.entity.CompetitionResult;
import com.aimlab.entity.CompetitionStatus;
import com.aimlab.entity.ShootingRecord;
import com.aimlab.mapper.CompetitionAthleteMapper;
import com.aimlab.mapper.CompetitionMapper;
import com.aimlab.mapper.CompetitionResultMapper;
import com.aimlab.mapper.ShootingRecordMapper;
import com.aimlab.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;

/**
 * 比赛服务类
 */
@Service
public class CompetitionService {

    @Autowired
    private CompetitionMapper competitionMapper;
    
    @Autowired
    private CompetitionAthleteMapper competitionAthleteMapper;
    
    @Autowired
    private ShootingRecordMapper shootingRecordMapper;
    
    @Autowired
    private CompetitionResultMapper competitionResultMapper;
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * 比赛状态缓存，使用内存存储当前正在进行的比赛状态
     * Key: 比赛ID, Value: 比赛状态对象
     */
    private final Map<Long, CompetitionStatus> competitionStatusMap = new ConcurrentHashMap<>();
    
    /**
     * 创建新比赛
     * 
     * @param competition 比赛对象
     * @return 创建的比赛对象
     */
    @Transactional
    public Competition createCompetition(Competition competition) {
        // 设置创建时间和初始状态
        competition.setCreatedAt(LocalDateTime.now());
        competition.setStatus("CREATED");
        
        // 保存比赛
        competitionMapper.insert(competition);
        
        return competition;
    }
    
    /**
     * 获取比赛详情
     * 
     * @param competitionId 比赛ID
     * @return 比赛对象
     */
    public Competition getCompetitionById(Integer competitionId) {
        return competitionMapper.findById(competitionId);
    }
    
    /**
     * 获取比赛列表
     * 
     * @return 比赛列表
     */
    public List<Competition> getAllCompetitions() {
        return competitionMapper.findAll();
    }
    
    /**
     * 更新比赛信息
     * 
     * @param competition 比赛对象
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition updateCompetition(Competition competition) {
        // 检查比赛是否存在
        Competition existingCompetition = competitionMapper.findById(competition.getId());
        if (existingCompetition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 不允许修改已开始的比赛
        if ("RUNNING".equals(existingCompetition.getStatus()) || 
            "PAUSED".equals(existingCompetition.getStatus())) {
            throw new RuntimeException("比赛已开始，无法修改基本信息");
        }
        
        // 更新比赛
        competitionMapper.update(competition);
        
        return competition;
    }
    
    /**
     * 删除比赛
     * 
     * @param competitionId 比赛ID
     */
    @Transactional
    public void deleteCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 不允许删除已开始的比赛
        if ("RUNNING".equals(competition.getStatus()) || 
            "PAUSED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已开始，无法删除");
        }
        
        // 删除比赛参赛运动员
        competitionAthleteMapper.deleteByCompetitionId(competitionId);
        
        // 删除比赛
        competitionMapper.delete(competitionId.longValue());
    }
    
    /**
     * 运动员报名参赛
     * 
     * @param competitionAthlete 比赛运动员对象
     * @return 报名结果
     */
    @Transactional
    public CompetitionAthlete registerAthlete(CompetitionAthlete competitionAthlete) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionAthlete.getCompetitionId());
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛是否已开始
        if ("RUNNING".equals(competition.getStatus()) || 
            "PAUSED".equals(competition.getStatus()) || 
            "COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已开始或已结束，无法报名");
        }
        
        // 检查运动员是否已报名
        CompetitionAthlete existingRegistration = competitionAthleteMapper.findByCompetitionIdAndAthleteId(
                competitionAthlete.getCompetitionId(), competitionAthlete.getAthleteId());
        if (existingRegistration != null) {
            throw new RuntimeException("运动员已报名该比赛");
        }
        
        // 设置报名时间
        competitionAthlete.setRegisteredAt(LocalDateTime.now());
        
        // 保存报名信息
        competitionAthleteMapper.insert(competitionAthlete);
        
        return competitionAthlete;
    }
    
    /**
     * 取消运动员报名
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     */
    @Transactional
    public void unregisterAthlete(Integer competitionId, Long athleteId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛是否已开始
        if ("RUNNING".equals(competition.getStatus()) || 
            "PAUSED".equals(competition.getStatus()) || 
            "COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已开始或已结束，无法取消报名");
        }
        
        // 检查运动员是否已报名
        CompetitionAthlete existingRegistration = competitionAthleteMapper.findByCompetitionIdAndAthleteId(
                competitionId, athleteId);
        if (existingRegistration == null) {
            throw new RuntimeException("运动员未报名该比赛");
        }
        
        // 删除报名信息
        competitionAthleteMapper.delete(existingRegistration.getId());
    }
    
    /**
     * 获取比赛参赛运动员列表
     * 
     * @param competitionId 比赛ID
     * @return 参赛运动员列表
     */
    public List<CompetitionAthlete> getCompetitionAthletes(Integer competitionId) {
        return competitionAthleteMapper.findByCompetitionId(competitionId);
    }
    
    /**
     * 开始比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition startCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if ("RUNNING".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已经开始");
        }
        if ("COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已经结束");
        }
        
        // 检查是否有参赛运动员
        List<CompetitionAthlete> athletes = competitionAthleteMapper.findByCompetitionId(competitionId);
        if (athletes.isEmpty()) {
            throw new RuntimeException("没有运动员报名，无法开始比赛");
        }
        
        // 更新比赛状态
        competition.setStatus("RUNNING");
        competition.setStartedAt(LocalDateTime.now());
        competitionMapper.update(competition);
        
        // 创建并存储比赛状态
        CompetitionStatus status = new CompetitionStatus();
        status.setCompetitionId(competitionId.longValue());
        status.setStartTime(competition.getStartedAt());
        status.setStatus("RUNNING");
        competitionStatusMap.put(competitionId.longValue(), status);
        
        // 通过WebSocket广播比赛开始消息
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "RUNNING", 
                "比赛已开始");
        
        return competition;
    }
    
    /**
     * 暂停比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition pauseCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if (!"RUNNING".equals(competition.getStatus())) {
            throw new RuntimeException("比赛未开始，无法暂停");
        }
        
        // 更新比赛状态
        competition.setStatus("PAUSED");
        competitionMapper.update(competition);
        
        // 更新内存中的比赛状态
        CompetitionStatus status = competitionStatusMap.get(competitionId.longValue());
        if (status != null) {
            status.setStatus("PAUSED");
            status.setPauseTime(LocalDateTime.now());
        }
        
        // 通过WebSocket广播比赛暂停消息
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "PAUSED", 
                "比赛已暂停");
        
        return competition;
    }
    
    /**
     * 恢复比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition resumeCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if (!"PAUSED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛未暂停，无法恢复");
        }
        
        // 更新比赛状态
        competition.setStatus("RUNNING");
        competitionMapper.update(competition);
        
        // 更新内存中的比赛状态
        CompetitionStatus status = competitionStatusMap.get(competitionId.longValue());
        if (status != null) {
            status.setStatus("RUNNING");
            status.setResumeTime(LocalDateTime.now());
        }
        
        // 通过WebSocket广播比赛恢复消息
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "RUNNING", 
                "比赛已恢复");
        
        return competition;
    }
    
    /**
     * 完成比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition completeCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if ("COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已经结束");
        }
        if (!"RUNNING".equals(competition.getStatus()) && !"PAUSED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛未开始，无法结束");
        }
        
        // 更新比赛状态
        competition.setStatus("COMPLETED");
        competition.setCompletedAt(LocalDateTime.now());
        
        // 计算比赛持续时间（秒）
        CompetitionStatus status = competitionStatusMap.get(competitionId.longValue());
        if (status != null) {
            long durationSeconds = status.calculateTotalDuration();
            competition.setDurationSeconds((int) durationSeconds);
        } else if (competition.getStartedAt() != null && competition.getCompletedAt() != null) {
            long durationSeconds = ChronoUnit.SECONDS.between(
                    competition.getStartedAt(), 
                    competition.getCompletedAt());
            competition.setDurationSeconds((int) durationSeconds);
        }
        
        competitionMapper.update(competition);
        
        // 计算并保存最终成绩
        calculateAndSaveFinalResults(competitionId);
        
        // 通过WebSocket广播比赛结束消息
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "COMPLETED", 
                "比赛已结束");
        
        return competition;
    }
    
    /**
     * 计算并保存最终比赛结果
     * 
     * @param competitionId 比赛ID
     */
    @Transactional
    private void calculateAndSaveFinalResults(Integer competitionId) {
        // 检查是否已经有结果记录
        int count = competitionResultMapper.countByCompetitionId(competitionId);
        if (count > 0) {
            // 如果已经有结果记录，先删除
            competitionResultMapper.deleteByCompetitionId(competitionId);
        }
        
        // 获取最终排名数据
        List<RankingItemDTO> rankings = getLiveRanking(competitionId);
        if (rankings == null || rankings.isEmpty()) {
            return; // 没有排名数据，直接返回
        }
        
        // 创建结果记录列表
        List<CompetitionResult> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 遍历排名数据，创建结果记录
        for (int i = 0; i < rankings.size(); i++) {
            RankingItemDTO ranking = rankings.get(i);
            
            CompetitionResult result = new CompetitionResult();
            result.setCompetitionId(competitionId);
            result.setAthleteId(ranking.getAthleteId());
            result.setAthleteName(ranking.getAthleteName());
            result.setFinalRank(i + 1); // 排名从1开始
            result.setFinalScore(ranking.getTotalScore());
            result.setTotalShots(ranking.getTotalShots());
            result.setCreatedAt(now);
            
            results.add(result);
        }
        
        // 批量保存结果记录
        if (!results.isEmpty()) {
            competitionResultMapper.batchInsert(results);
        }
    }
    
    /**
     * 获取比赛最终结果
     * 
     * @param competitionId 比赛ID
     * @return 比赛结果列表
     */
    public List<CompetitionResult> getCompetitionResults(Integer competitionId) {
        return competitionResultMapper.findByCompetitionId(competitionId);
    }
    
    /**
     * 获取运动员的比赛结果
     * 
     * @param athleteId 运动员ID
     * @return 比赛结果列表
     */
    public List<CompetitionResult> getAthleteResults(Long athleteId) {
        return competitionResultMapper.findByAthleteId(athleteId);
    }
    
    /**
     * 添加比赛射击记录
     * 
     * @param record 射击记录对象
     * @return 添加的记录对象
     */
    @Transactional
    public ShootingRecord addCompetitionRecord(ShootingRecord record) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(record.getCompetitionId());
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛是否正在进行
        if (!"RUNNING".equals(competition.getStatus())) {
            throw new RuntimeException("比赛未开始，无法记录成绩");
        }
        
        // 检查运动员是否报名参加该比赛
        CompetitionAthlete athlete = competitionAthleteMapper.findByCompetitionIdAndAthleteId(
                record.getCompetitionId(), record.getAthleteId());
        if (athlete == null) {
            throw new RuntimeException("运动员未报名参加该比赛");
        }
        
        // 保存射击记录
        record.setShotAt(LocalDateTime.now());
        shootingRecordMapper.insert(record);
        
        // 更新排名并广播
        updateAndBroadcastRankings(record.getCompetitionId().longValue());
        
        return record;
    }
    
    /**
     * 获取比赛的射击记录
     * 
     * @param competitionId 比赛ID
     * @return 射击记录列表
     */
    public List<ShootingRecord> getCompetitionRecords(Integer competitionId) {
        return shootingRecordMapper.findByCompetitionId(competitionId);
    }
    
    /**
     * 获取比赛中运动员的射击记录
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     * @return 射击记录列表
     */
    public List<ShootingRecord> getAthleteCompetitionRecords(Integer competitionId, Long athleteId) {
        return shootingRecordMapper.findByCompetitionIdAndAthleteId(competitionId, athleteId);
    }
    
    /**
     * 获取比赛实时排名
     * 
     * @param competitionId 比赛ID
     * @return 排名列表
     */
    public List<RankingItemDTO> getLiveRanking(Integer competitionId) {
        // 实现实时排名计算逻辑
        // 根据比赛ID获取所有射击记录，计算每个运动员的总分和排名
        return calculateRanking(competitionId);
    }
    
    /**
     * 更新并广播比赛排名
     * 
     * @param competitionId 比赛ID
     */
    private void updateAndBroadcastRankings(Long competitionId) {
        // 计算最新排名
        List<RankingItemDTO> rankings = calculateRanking(competitionId.intValue());
        
        // 通过WebSocket广播最新排名
        webSocketService.sendRankingUpdate(String.valueOf(competitionId), rankings);
    }
    
    private List<RankingItemDTO> calculateRanking(Integer competitionId) {
        // 获取比赛的所有参赛运动员
        List<CompetitionAthlete> athletes = competitionAthleteMapper.findByCompetitionId(competitionId);
        
        // 获取比赛的所有射击记录
        List<ShootingRecord> records = shootingRecordMapper.findByCompetitionId(competitionId);
        
        // 计算每个运动员的总分
        Map<Long, RankingItemDTO> athleteScores = new HashMap<>();
        
        for (CompetitionAthlete athlete : athletes) {
            RankingItemDTO rankingItem = new RankingItemDTO();
            rankingItem.setAthleteId(athlete.getAthleteId());
            rankingItem.setAthleteName(athlete.getAthleteName());
            rankingItem.setTotalScore(BigDecimal.ZERO);
            rankingItem.setTotalShots(0);
            athleteScores.put(athlete.getAthleteId(), rankingItem);
        }
        
        // 计算每个运动员的总分和排名
        for (ShootingRecord record : records) {
            RankingItemDTO rankingItem = athleteScores.get(record.getAthleteId());
            if (rankingItem != null) {
                rankingItem.setTotalScore(rankingItem.getTotalScore().add(record.getScore()));
                rankingItem.setTotalShots(rankingItem.getTotalShots() + 1);
                rankingItem.setLastShotTime(record.getShotAt());
            }
        }
        
        // 将结果转换为RankingItemDTO列表
        List<RankingItemDTO> rankings = new ArrayList<>(athleteScores.values());
        
        // 按总分降序排序
        rankings.sort((r1, r2) -> r2.getTotalScore().compareTo(r1.getTotalScore()));
        
        // 添加排名信息
        for (int i = 0; i < rankings.size(); i++) {
            // 排名从1开始
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings;
    }
    
    /**
     * 批量报名运动员
     * 
     * @param competitionId 比赛ID
     * @param athleteIds 运动员ID列表
     * @return 成功报名的运动员数量
     */
    @Transactional
    public int enrollAthletes(Integer competitionId, List<Long> athleteIds) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛是否已开始
        if ("RUNNING".equals(competition.getStatus()) || 
            "PAUSED".equals(competition.getStatus()) || 
            "COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已开始或已结束，无法报名");
        }
        
        int enrolledCount = 0;
        for (Long athleteId : athleteIds) {
            // 检查运动员是否已报名
            CompetitionAthlete existingRegistration = competitionAthleteMapper.findByCompetitionIdAndAthleteId(
                    competitionId, athleteId);
            if (existingRegistration != null) {
                continue; // 已报名，跳过
            }
            
            // 创建新的报名记录
            CompetitionAthlete competitionAthlete = new CompetitionAthlete();
            competitionAthlete.setCompetitionId(competitionId);
            competitionAthlete.setAthleteId(athleteId);
            competitionAthlete.setStatus("ENROLLED");
            competitionAthlete.setRegisteredAt(LocalDateTime.now());
            competitionAthlete.setCreatedAt(LocalDateTime.now());
            
            // 保存报名信息
            competitionAthleteMapper.insert(competitionAthlete);
            enrolledCount++;
        }
        
        return enrolledCount;
    }
    
    /**
     * 取消比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition cancelCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if ("COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已经结束，无法取消");
        }
        
        // 更新比赛状态
        competition.setStatus("CANCELED");
        competitionMapper.update(competition);
        
        // 如果比赛正在进行中，从内存中移除比赛状态
        competitionStatusMap.remove(competitionId.longValue());
        
        // 通过WebSocket广播比赛取消消息
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "CANCELED", 
                "比赛已取消");
        
        return competition;
    }
    
    /**
     * 获取比赛实时状态
     * 
     * @param competitionId 比赛ID
     * @return 比赛实时状态
     */
    public CompetitionStatus getCompetitionStatus(Integer competitionId) {
        return competitionStatusMap.get(competitionId.longValue());
    }
    
    /**
     * 根据状态获取比赛列表
     * 
     * @param status 比赛状态
     * @return 比赛列表
     */
    public List<Competition> getCompetitionsByStatus(String status) {
        return competitionMapper.findByStatus(status);
    }
    
    /**
     * 获取当前用户创建的比赛列表
     * 
     * @return 比赛列表
     */
    public List<Competition> getMyCreatedCompetitions() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询用户创建的比赛
        return competitionMapper.findByCreatedBy(userId);
    }
} 