package com.aimlab.mapper;

import com.aimlab.entity.CompetitionAthlete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 比赛运动员关联Mapper接口
 */
@Mapper
public interface CompetitionAthleteMapper {
    
    /**
     * 插入比赛运动员关联
     * 
     * @param competitionAthlete 比赛运动员关联对象
     * @return 影响的行数
     */
    int insert(CompetitionAthlete competitionAthlete);
    
    /**
     * 批量插入比赛运动员关联
     * 
     * @param competitionAthletes 比赛运动员关联对象列表
     * @return 影响的行数
     */
    int batchInsert(List<CompetitionAthlete> competitionAthletes);
    
    /**
     * 根据比赛ID和运动员ID查询关联
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     * @return 比赛运动员关联对象，如果不存在则返回null
     */
    CompetitionAthlete findByCompetitionIdAndAthleteId(@Param("competitionId") Integer competitionId, @Param("athleteId") Long athleteId);
    
    /**
     * 根据比赛ID查询所有参赛运动员关联
     * 
     * @param competitionId 比赛ID
     * @return 比赛运动员关联对象列表
     */
    List<CompetitionAthlete> findByCompetitionId(@Param("competitionId") Integer competitionId);
    
    /**
     * 根据运动员ID查询所有参赛比赛关联
     * 
     * @param athleteId 运动员ID
     * @return 比赛运动员关联对象列表
     */
    List<CompetitionAthlete> findByAthleteId(@Param("athleteId") Long athleteId);
    
    /**
     * 更新比赛运动员关联状态
     * 
     * @param id 关联ID
     * @param status 状态
     * @return 影响的行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 删除比赛运动员关联
     * 
     * @param id 关联ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 删除比赛运动员关联
     * 
     * @param id 关联ID
     * @return 影响的行数
     */
    int delete(@Param("id") Long id);
    
    /**
     * 删除比赛的所有运动员关联
     * 
     * @param competitionId 比赛ID
     * @return 影响的行数
     */
    int deleteByCompetitionId(@Param("competitionId") Integer competitionId);
} 