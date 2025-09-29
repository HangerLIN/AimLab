package com.aimlab.mapper;

import com.aimlab.entity.TrainingSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 训练场次Mapper接口
 */
@Mapper
public interface TrainingSessionMapper {
    
    /**
     * 插入新训练场次
     * 
     * @param trainingSession 训练场次对象
     * @return 影响的行数
     */
    int insert(TrainingSession trainingSession);
    
    /**
     * 根据ID查询训练场次
     * 
     * @param id 训练场次ID
     * @return 训练场次对象，如果不存在则返回null
     */
    TrainingSession findById(@Param("id") Long id);
    
    /**
     * 根据运动员ID查询训练场次列表
     * 
     * @param athleteId 运动员ID
     * @return 训练场次列表
     */
    List<TrainingSession> findByAthleteId(@Param("athleteId") Long athleteId);
    
    /**
     * 更新训练场次信息
     * 
     * @param trainingSession 训练场次对象
     * @return 影响的行数
     */
    int update(TrainingSession trainingSession);
    
    /**
     * 结束训练场次（设置结束时间）
     * 
     * @param id 训练场次ID
     * @param endTime 结束时间
     * @return 影响的行数
     */
    int endSession(@Param("id") Long id, @Param("endTime") java.time.LocalDateTime endTime);
} 