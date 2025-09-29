package com.aimlab.mapper;

import com.aimlab.entity.CompetitionResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 比赛结果Mapper接口
 */
@Mapper
public interface CompetitionResultMapper {
    
    /**
     * 插入比赛结果
     * 
     * @param result 比赛结果
     * @return 影响的行数
     */
    int insert(CompetitionResult result);
    
    /**
     * 批量插入比赛结果
     * 
     * @param results 比赛结果列表
     * @return 影响的行数
     */
    int batchInsert(List<CompetitionResult> results);
    
    /**
     * 根据比赛ID查询比赛结果
     * 
     * @param competitionId 比赛ID
     * @return 比赛结果列表
     */
    List<CompetitionResult> findByCompetitionId(@Param("competitionId") Integer competitionId);
    
    /**
     * 根据比赛ID和运动员ID查询比赛结果
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     * @return 比赛结果
     */
    CompetitionResult findByCompetitionIdAndAthleteId(
            @Param("competitionId") Integer competitionId, 
            @Param("athleteId") Long athleteId);
    
    /**
     * 根据运动员ID查询比赛结果
     * 
     * @param athleteId 运动员ID
     * @return 比赛结果列表
     */
    List<CompetitionResult> findByAthleteId(@Param("athleteId") Long athleteId);
    
    /**
     * 检查比赛是否已有结果记录
     * 
     * @param competitionId 比赛ID
     * @return 结果记录数量
     */
    int countByCompetitionId(@Param("competitionId") Integer competitionId);
    
    /**
     * 删除比赛的所有结果记录
     * 
     * @param competitionId 比赛ID
     * @return 影响的行数
     */
    int deleteByCompetitionId(@Param("competitionId") Integer competitionId);
} 