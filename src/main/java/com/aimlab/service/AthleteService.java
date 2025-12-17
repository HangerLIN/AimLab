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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private MessageService messageService;
    
    /**
     * 获取所有运动员列表
     * 
     * @return 所有运动员列表
     */
    public List<Athlete> getAllAthletes() {
        return athleteMapper.findAll();
    }
    
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
        
        // 通知管理员有新运动员注册待审核
        if ("PENDING".equals(athlete.getApprovalStatus())) {
            messageService.notifyAdminsNewAthleteRegistration(athlete.getName());
        }
        
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
     * 如果运动员档案已批准，修改会标记为待审批状态，原数据保持不变
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
        
        // 如果审批状态为空，视为已审批档案，后续修改需要审批
        String existingApprovalStatus = existingAthlete.getApprovalStatus();
        if (existingApprovalStatus == null) {
            existingApprovalStatus = "APPROVED";
            existingAthlete.setApprovalStatus("APPROVED");
        }

        // 如果档案已经批准，需要特殊处理
        if ("APPROVED".equals(existingApprovalStatus)) {
            // 检查是否有实际的字段修改
            if (hasAthleteInfoChanged(existingAthlete, athlete)) {
                // 将修改数据序列化为 JSON 存储
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    Map<String, Object> modificationData = new HashMap<>();
                    modificationData.put("name", athlete.getName());
                    modificationData.put("gender", athlete.getGender());
                    modificationData.put("birthDate", athlete.getBirthDate() != null ? athlete.getBirthDate().toString() : null);
                    modificationData.put("level", athlete.getLevel());
                    String jsonData = objectMapper.writeValueAsString(modificationData);
                    
                    // 只记录修改为待审批，不保存新的数据字段
                    existingAthlete.setModificationStatus("PENDING");
                    existingAthlete.setPendingModificationData(jsonData);
                    existingAthlete.setUpdatedAt(LocalDateTime.now());
                    // 保存修改标记和待审批数据
                    athleteMapper.update(existingAthlete);
                    
                    // 通知管理员有运动员档案修改待审核
                    messageService.notifyAdminsAthleteModification(existingAthlete.getName());
                    
                    return true;
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("序列化修改数据失败: " + e.getMessage());
                }
            } else {
                // 没有实际修改
                return false;
            }
        } else {
            // 未批准的档案可以直接修改
            athlete.setUpdatedAt(LocalDateTime.now());
            if (athlete.getApprovalStatus() == null) {
                athlete.setApprovalStatus(existingApprovalStatus);
            }
            athlete.setModificationStatus(null);
            athlete.setPendingModificationData(null);
            return athleteMapper.update(athlete) > 0;
        }
    }
    
    /**
     * 更新运动员头像（BLOB存储）
     * 
     * @param athleteId 运动员ID
     * @param avatarData 头像二进制数据
     * @param avatarType 头像MIME类型
     * @return 更新成功返回true
     */
    @Transactional
    public boolean updateAvatar(Long athleteId, byte[] avatarData, String avatarType) {
        return athleteMapper.updateAvatar(athleteId, avatarData, avatarType) > 0;
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
        
        // 检查运动员档案是否已被批准
        if (athlete.getApprovalStatus() == null || !"APPROVED".equals(athlete.getApprovalStatus())) {
            throw new RuntimeException("运动员档案未被批准，无法查看详细资料");
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

    /**
     * 更新运动员审批状态
     * 
     * @param athleteId 运动员ID
     * @param status 审批状态 (PENDING, APPROVED, REJECTED)
     */
    @Transactional
    public void updateApprovalStatus(Long athleteId, String status) {
        updateApprovalStatus(athleteId, status, null);
    }
    
    /**
     * 更新运动员审批状态（带拒绝原因）
     * 
     * @param athleteId 运动员ID
     * @param status 审批状态 (PENDING, APPROVED, REJECTED)
     * @param reason 拒绝原因（仅当状态为REJECTED时使用）
     */
    @Transactional
    public void updateApprovalStatus(Long athleteId, String status, String reason) {
        Athlete athlete = athleteMapper.findById(athleteId);
        if (athlete == null) {
            throw new RuntimeException("运动员不存在");
        }
        athlete.setApprovalStatus(status);
        athlete.setUpdatedAt(LocalDateTime.now());
        athleteMapper.update(athlete);
        
        // 发送站内信通知
        if ("APPROVED".equals(status)) {
            messageService.sendAthleteApprovalNotification(athlete.getUserId(), athlete.getName());
        } else if ("REJECTED".equals(status)) {
            messageService.sendAthleteRejectionNotification(athlete.getUserId(), athlete.getName(), reason);
        }
    }

    /**
     * 获取待审批的运动员列表
     * 
     * @param limit 返回数量限制
     * @return 待审批运动员列表
     */
    public List<Athlete> getPendingAthletes(Integer limit) {
        return athleteMapper.findByApprovalStatus("PENDING", limit);
    }

    /**
     * 获取已批准的运动员列表
     * 
     * @param limit 返回数量限制
     * @return 已批准运动员列表
     */
    public List<Athlete> getApprovedAthletes(Integer limit) {
        return athleteMapper.findByApprovalStatus("APPROVED", limit);
    }

    /**
     * 统计待审批的运动员数量
     * 
     * @return 待审批运动员数量
     */
    public long countPendingAthletes() {
        return athleteMapper.countByApprovalStatus("PENDING");
    }

    /**
     * 统计已批准的运动员数量
     * 
     * @return 已批准运动员数量
     */
    public long countApprovedAthletes() {
        return athleteMapper.countByApprovalStatus("APPROVED");
    }

    /**
     * 删除运动员
     * 
     * @param athleteId 运动员ID
     */
    @Transactional
    public void deleteAthlete(Long athleteId) {
        Athlete athlete = athleteMapper.findById(athleteId);
        if (athlete == null) {
            throw new RuntimeException("运动员不存在");
        }
        athleteMapper.deleteById(athleteId);
    }
    
    /**
     * 检查运动员档案信息是否有变化
     * 
     * @param existing 现有的运动员信息
     * @param updated 更新的运动员信息
     * @return 如果有变化返回true，否则返回false
     */
    private boolean hasAthleteInfoChanged(Athlete existing, Athlete updated) {
        // 检查基本信息是否有变化
        if (!stringEquals(existing.getName(), updated.getName())) {
            return true;
        }
        if (!stringEquals(existing.getGender(), updated.getGender())) {
            return true;
        }
        if (!dateEquals(existing.getBirthDate(), updated.getBirthDate())) {
            return true;
        }
        if (!stringEquals(existing.getLevel(), updated.getLevel())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 比较两个字符串是否相等（处理null情况）
     */
    private boolean stringEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    /**
     * 比较两个日期是否相等（处理null情况）
     */
    private boolean dateEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * 获取待审批修改的运动员列表
     * 
     * @return 待审批修改的运动员列表
     */
    public List<Athlete> getPendingModificationAthletes() {
        return athleteMapper.findByModificationStatus("PENDING");
    }

    /**
     * 审批通过运动员档案修改
     * 将待审批的修改实际应用到档案数据中
     * 
     * @param athleteId 运动员ID
     * @param modificationData 修改的数据（可选，如果为空则从 pendingModificationData 读取）
     * @return 是否成功
     */
    @Transactional
    public boolean approveModification(Long athleteId, Athlete modificationData) {
        Athlete athlete = athleteMapper.findById(athleteId);
        if (athlete == null) {
            throw new RuntimeException("运动员不存在");
        }

        if (!"PENDING".equals(athlete.getModificationStatus())) {
            throw new RuntimeException("该运动员档案无待审批的修改");
        }

        // 从 pendingModificationData 中读取待审批的修改数据
        String pendingData = athlete.getPendingModificationData();
        if (pendingData != null && !pendingData.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = objectMapper.readValue(pendingData, Map.class);
                
                // 应用修改到档案
                if (dataMap.get("name") != null) {
                    athlete.setName((String) dataMap.get("name"));
                }
                if (dataMap.get("gender") != null) {
                    athlete.setGender((String) dataMap.get("gender"));
                }
                if (dataMap.get("birthDate") != null) {
                    String birthDateStr = (String) dataMap.get("birthDate");
                    athlete.setBirthDate(java.time.LocalDate.parse(birthDateStr));
                }
                if (dataMap.get("level") != null) {
                    athlete.setLevel((String) dataMap.get("level"));
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("解析修改数据失败: " + e.getMessage());
            }
        } else if (modificationData != null) {
            // 如果没有存储的数据，使用传入的数据
            if (modificationData.getName() != null) {
                athlete.setName(modificationData.getName());
            }
            if (modificationData.getGender() != null) {
                athlete.setGender(modificationData.getGender());
            }
            if (modificationData.getBirthDate() != null) {
                athlete.setBirthDate(modificationData.getBirthDate());
            }
            if (modificationData.getLevel() != null) {
                athlete.setLevel(modificationData.getLevel());
            }
        }

        // 清除修改状态和待审批数据，标记为已批准
        athlete.setModificationStatus("APPROVED");
        athlete.setPendingModificationData(null);
        athlete.setUpdatedAt(LocalDateTime.now());

        boolean success = athleteMapper.update(athlete) > 0;
        
        // 发送站内信通知
        if (success) {
            messageService.sendModificationApprovalNotification(athlete.getUserId(), athlete.getName());
        }
        
        return success;
    }

    /**
     * 拒绝运动员档案修改
     * 直接清除修改标记，不应用任何数据修改
     * 
     * @param athleteId 运动员ID
     * @return 是否成功
     */
    @Transactional
    public boolean rejectModification(Long athleteId) {
        return rejectModification(athleteId, null);
    }
    
    /**
     * 拒绝运动员档案修改（带拒绝原因）
     * 直接清除修改标记，不应用任何数据修改
     * 
     * @param athleteId 运动员ID
     * @param reason 拒绝原因
     * @return 是否成功
     */
    @Transactional
    public boolean rejectModification(Long athleteId, String reason) {
        Athlete athlete = athleteMapper.findById(athleteId);
        if (athlete == null) {
            throw new RuntimeException("运动员不存在");
        }

        if (!"PENDING".equals(athlete.getModificationStatus())) {
            throw new RuntimeException("该运动员档案无待审批的修改");
        }

        // 清除修改状态和待审批数据，标记为已拒绝
        athlete.setModificationStatus("REJECTED");
        athlete.setPendingModificationData(null);
        athlete.setUpdatedAt(LocalDateTime.now());

        boolean success = athleteMapper.update(athlete) > 0;
        
        // 发送站内信通知
        if (success) {
            messageService.sendModificationRejectionNotification(athlete.getUserId(), athlete.getName(), reason);
        }
        
        return success;
    }
} 
