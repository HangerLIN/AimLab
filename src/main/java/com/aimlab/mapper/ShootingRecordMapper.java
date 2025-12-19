package com.aimlab.mapper;

import com.aimlab.dto.RankingItemDTO;
import com.aimlab.entity.ShootingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 射击记录Mapper接口
 */
@Mapper
public interface ShootingRecordMapper {
    
    /**
     * 插入射击记录
     * 
     * @param record 射击记录对象
     * @return 影响的行数
     */
    int insert(ShootingRecord record);
    
    /**
     * 根据ID查询射击记录
     * 
     * @param id 记录ID
     * @return 射击记录对象
     */
    ShootingRecord findById(@Param("id") Long id);
    
    /**
     * 根据运动员ID查询射击记录
     * 
     * @param athleteId 运动员ID
     * @param limit 限制返回记录数
     * @return 射击记录列表
     */
    List<ShootingRecord> findByAthleteId(@Param("athleteId") Long athleteId, @Param("limit") int limit);
    
    /**
     * 根据比赛ID查询射击记录
     * 
     * @param competitionId 比赛ID
     * @return 射击记录列表
     */
    List<ShootingRecord> findByCompetitionId(@Param("competitionId") Integer competitionId);
    
    /**
     * 根据训练场次ID查询射击记录
     * 
     * @param trainingSessionId 训练场次ID
     * @return 射击记录列表
     */
    List<ShootingRecord> findByTrainingSessionId(@Param("trainingSessionId") Long trainingSessionId);
    
    /**
     * 根据比赛ID和运动员ID查询射击记录
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     * @return 射击记录列表
     */
    List<ShootingRecord> findByCompetitionIdAndAthleteId(
            @Param("competitionId") Integer competitionId, 
            @Param("athleteId") Long athleteId);
    
    /**
     * 根据比赛ID、运动员ID和轮次查询射击记录
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     * @param roundNumber 轮次
     * @return 射击记录列表
     */
    List<ShootingRecord> findByCompetitionIdAndAthleteIdAndRound(
            @Param("competitionId") Integer competitionId, 
            @Param("athleteId") Long athleteId,
            @Param("roundNumber") Integer roundNumber);
    
    /**
     * 查询运动员在指定时间范围内的射击记录
     * 
     * @param athleteId 运动员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 射击记录列表
     */
    List<ShootingRecord> findByAthleteIdAndTimeRange(
            @Param("athleteId") Long athleteId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取比赛实时排名
     * 
     * @param competitionId 比赛ID
     * @return 排名列表
     */
    List<RankingItemDTO> getLiveRanking(@Param("competitionId") Integer competitionId);
    
    /**
     * 统计运动员在指定比赛中的射击次数
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     * @return 射击次数
     */
    int countByCompetitionIdAndAthleteId(
            @Param("competitionId") Integer competitionId, 
            @Param("athleteId") Long athleteId);
    
    /**
     * 获取所有射击记录的平均成绩
     * 
     * @return 平均成绩
     */
    java.math.BigDecimal getAverageScore();
    
    /**
     * 按运动员统计训练次数和平均成绩
     * 
     * @return 运动员训练统计列表
     */
    List<java.util.Map<String, Object>> getAthleteTrainingStats();
    
    /**
     * 获取成绩分布统计（按分数段）
     * 
     * @return 成绩分布列表
     */
    List<java.util.Map<String, Object>> getScoreDistribution();
} 