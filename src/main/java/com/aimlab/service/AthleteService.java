package com.aimlab.service;

import com.aimlab.dto.AthleteHistoryItemDTO;
import com.aimlab.dto.AthleteProfileDTO;
import com.aimlab.entity.Athlete;
import com.aimlab.entity.CompetitionAthlete;
import com.aimlab.entity.CompetitionResult;
import com.aimlab.entity.ShootingRecord;
import com.aimlab.entity.TrainingSession;
import com.aimlab.entity.User;
import com.aimlab.mapper.AthleteMapper;
import com.aimlab.mapper.CompetitionAthleteMapper;
import com.aimlab.mapper.CompetitionMapper;
import com.aimlab.mapper.CompetitionResultMapper;
import com.aimlab.mapper.ShootingRecordMapper;
import com.aimlab.mapper.TrainingSessionMapper;
import com.aimlab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运动员服务类
 */
@Service
public class AthleteService {

    @Autowired
    private AthleteMapper athleteMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TrainingSessionMapper trainingSessionMapper;
    
    @Autowired
    private CompetitionAthleteMapper competitionAthleteMapper;
    
    @Autowired
    private CompetitionMapper competitionMapper;
    
    @Autowired
    private CompetitionResultMapper competitionResultMapper;
    
    @Autowired
    private ShootingRecordMapper shootingRecordMapper;
    
    /**
     * 创建运动员档案
     * 
     * @param athlete 运动员信息
     * @return 创建成功返回运动员ID，失败返回null
     */
    @Transactional
    public Long createAthleteProfile(Athlete athlete) {
        // 检查用户是否存在
        User user = userMapper.findById(athlete.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查该用户是否已经创建过运动员档案
        Athlete existingAthlete = athleteMapper.findByUserId(athlete.getUserId());
        if (existingAthlete != null) {
            throw new RuntimeException("该用户已创建运动员档案");
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        athlete.setCreatedAt(now);
        athlete.setUpdatedAt(now);
        
        // 如果性别为空，设置默认值
        if (athlete.getGender() == null) {
            athlete.setGender("UNKNOWN");
        }

        if (athlete.getApprovalStatus() == null) {
            athlete.setApprovalStatus("PENDING");
        }
        
        // 保存运动员信息
        athleteMapper.insert(athlete);
        
        return athlete.getId();
    }
    
    /**
     * 根据ID获取运动员信息
     * 
     * @param id 运动员ID
     * @return 运动员对象
     */
    public Athlete getAthleteById(Long id) {
        return athleteMapper.findById(id);
    }
    
    /**
     * 根据用户ID获取运动员信息
     * 
     * @param userId 用户ID
     * @return 运动员对象
     */
    public Athlete getAthleteByUserId(Long userId) {
        return athleteMapper.findByUserId(userId);
    }
    
    /**
     * 更新运动员信息
     * 
     * @param athlete 运动员信息
     * @return 更新成功返回true，失败返回false
     */
    @Transactional
    public boolean updateAthleteProfile(Athlete athlete) {
        // 检查运动员是否存在
        Athlete existingAthlete = athleteMapper.findById(athlete.getId());
        if (existingAthlete == null) {
            throw new RuntimeException("运动员不存在");
        }
        
        // 设置更新时间
        athlete.setUpdatedAt(LocalDateTime.now());

        // 如果未传入审批状态，沿用现有状态
        if (athlete.getApprovalStatus() == null) {
            athlete.setApprovalStatus(existingAthlete.getApprovalStatus());
        }
        
        // 更新运动员信息
        return athleteMapper.update(athlete) > 0;
    }
    
    /**
     * 获取运动员个人资料，包括历史记录和生涯统计
     * 
     * @param athleteId 运动员ID
     * @return 运动员个人资料DTO
     */
    public AthleteProfileDTO getAthleteProfile(Long athleteId) {
        // 1. 查询基本信息
        Athlete athlete = athleteMapper.findById(athleteId);
        if (athlete == null) {
            throw new RuntimeException("运动员不存在");
        }
        
        // 创建个人资料DTO并设置基本信息
        AthleteProfileDTO profileDTO = AthleteProfileDTO.fromAthlete(athlete);
        
        // 2. 查询训练历史
        List<TrainingSession> trainingSessions = trainingSessionMapper.findByAthleteId(athleteId);
        List<AthleteHistoryItemDTO> trainingHistory = new ArrayList<>();
        
        // 训练统计数据
        int totalTrainingSessions = trainingSessions.size();
        long totalTrainingMinutes = 0;
        
        for (TrainingSession session : trainingSessions) {
            AthleteHistoryItemDTO historyItem = new AthleteHistoryItemDTO();
            historyItem.setType("TRAINING");
            historyItem.setId(session.getId());
            historyItem.setName(session.getSessionName());
            historyItem.setDate(session.getStartTime());
            
            // 计算训练时长
            if (session.getStartTime() != null && session.getEndTime() != null) {
                long durationMinutes = Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
                totalTrainingMinutes += durationMinutes;
            }
            
            // 获取训练射击记录，计算平均分
            List<ShootingRecord> records = shootingRecordMapper.findByTrainingSessionId(session.getId());
            if (!records.isEmpty()) {
                BigDecimal totalScore = records.stream()
                        .map(ShootingRecord::getScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal averageScore = totalScore.divide(new BigDecimal(records.size()), 2, RoundingMode.HALF_UP);
                
                historyItem.setResult("平均环数: " + averageScore);
                historyItem.setResultValue(averageScore);
            } else {
                historyItem.setResult("无记录");
                historyItem.setResultValue(BigDecimal.ZERO);
            }
            
            trainingHistory.add(historyItem);
        }
        
        // 3. 查询比赛历史
        List<CompetitionAthlete> competitionAthletes = competitionAthleteMapper.findByAthleteId(athleteId);
        List<AthleteHistoryItemDTO> competitionHistory = new ArrayList<>();
        
        // 比赛统计数据
        int totalCompetitions = competitionAthletes.size();
        int competitionsWon = 0;
        int competitionsTopThree = 0;
        
        for (CompetitionAthlete competitionAthlete : competitionAthletes) {
            Integer competitionId = competitionAthlete.getCompetitionId();
            
            // 获取比赛信息
            com.aimlab.entity.Competition competition = competitionMapper.findById(competitionId);
            if (competition == null) {
                continue;
            }
            
            // 获取比赛结果
            CompetitionResult result = competitionResultMapper.findByCompetitionIdAndAthleteId(competitionId, athleteId);
            
            AthleteHistoryItemDTO historyItem = new AthleteHistoryItemDTO();
            historyItem.setType("COMPETITION");
            historyItem.setId(Long.valueOf(competitionId));
            historyItem.setName(competition.getName());
            historyItem.setDate(competition.getStartedAt());
            
            if (result != null) {
                historyItem.setRank(result.getFinalRank());
                historyItem.setResult("排名: " + result.getFinalRank() + ", 得分: " + result.getFinalScore());
                historyItem.setResultValue(result.getFinalScore());
                
                // 统计获奖情况
                if (result.getFinalRank() == 1) {
                    competitionsWon++;
                }
                if (result.getFinalRank() <= 3) {
                    competitionsTopThree++;
                }
            } else {
                historyItem.setResult("未完成");
            }
            
            competitionHistory.add(historyItem);
        }
        
        // 4. 合并训练和比赛历史，并按日期倒序排序
        List<AthleteHistoryItemDTO> allHistory = new ArrayList<>();
        allHistory.addAll(trainingHistory);
        allHistory.addAll(competitionHistory);
        // 使用 nullsLast 处理 null 日期，确保排序不会抛出异常
        allHistory.sort(Comparator.comparing(AthleteHistoryItemDTO::getDate, 
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        
        profileDTO.setHistoryItems(allHistory);
        
        // 5. 计算生涯统计数据
        // 查询所有射击记录
        List<ShootingRecord> allRecords = shootingRecordMapper.findByAthleteId(athleteId, 10000); // 设置一个较大的限制
        
        int careerTotalShots = allRecords.size();
        BigDecimal careerBestScore = BigDecimal.ZERO;
        BigDecimal careerTotalScore = BigDecimal.ZERO;
        
        if (!allRecords.isEmpty()) {
            careerTotalScore = allRecords.stream()
                    .map(ShootingRecord::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            careerBestScore = allRecords.stream()
                    .map(ShootingRecord::getScore)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        }
        
        BigDecimal careerAverageScore = careerTotalShots > 0 
                ? careerTotalScore.divide(new BigDecimal(careerTotalShots), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;
        
        // 设置生涯统计数据
        profileDTO.setCareerTotalShots(careerTotalShots);
        profileDTO.setCareerAverageScore(careerAverageScore);
        profileDTO.setCareerBestScore(careerBestScore);
        profileDTO.setTotalCompetitions(totalCompetitions);
        profileDTO.setCompetitionsWon(competitionsWon);
        profileDTO.setCompetitionsTopThree(competitionsTopThree);
        profileDTO.setTotalTrainingSessions(totalTrainingSessions);
        profileDTO.setTotalTrainingMinutes(totalTrainingMinutes);
        
        return profileDTO;
    }
} 
