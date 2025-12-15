package com.aimlab.mapper;

import com.aimlab.dto.AthleteCompareDTO;
import com.aimlab.dto.TrendDataDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdvancedAnalyticsMapper {

    List<AthleteCompareDTO> getAthleteCompareData(
            @Param("level") String level,
            @Param("athleteIds") List<Long> athleteIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("projectType") String projectType,
            @Param("limit") Integer limit);

    List<TrendDataDTO> getWeeklyTrendData(
            @Param("athleteId") Long athleteId,
            @Param("weeks") Integer weeks,
            @Param("projectType") String projectType);

    List<AthleteCompareDTO> getAthletesByIds(
            @Param("athleteIds") List<Long> athleteIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("projectType") String projectType);
}
