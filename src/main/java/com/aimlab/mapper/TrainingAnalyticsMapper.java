package com.aimlab.mapper;

import com.aimlab.dto.TrainingAthleteStatsDTO;
import com.aimlab.dto.TrainingProjectStatsDTO;
import com.aimlab.dto.TrainingTimeStatsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练统计分析 Mapper
 */
@Mapper
public interface TrainingAnalyticsMapper {

    List<TrainingTimeStatsDTO> aggregateByTime(@Param("granularity") String granularity,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("athleteId") Long athleteId,
                                               @Param("projectType") String projectType);

    List<TrainingAthleteStatsDTO> aggregateByAthlete(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime,
                                                     @Param("projectType") String projectType,
                                                     @Param("keyword") String keyword);

    List<TrainingProjectStatsDTO> aggregateByProject(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime,
                                                     @Param("athleteId") Long athleteId);
}
