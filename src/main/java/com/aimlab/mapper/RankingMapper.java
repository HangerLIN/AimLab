package com.aimlab.mapper;

import com.aimlab.dto.RankingDTO;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 排行榜数据访问接口
 */
public interface RankingMapper {
    
    /**
     * 获取全站排行榜（基于所有比赛成绩）
     * @param limit 返回条数
     * @return 排行榜列表
     */
    List<RankingDTO> getOverallRanking(@Param("limit") Integer limit);
    
    /**
     * 获取分类排行榜（按运动员等级分类）
     * @param level 运动员等级
     * @param limit 返回条数
     * @return 排行榜列表
     */
    List<RankingDTO> getRankingByLevel(@Param("level") String level, @Param("limit") Integer limit);
    
    /**
     * 获取月度排行榜
     * @param year 年份
     * @param month 月份
     * @param limit 返回条数
     * @return 排行榜列表
     */
    List<RankingDTO> getMonthlyRanking(@Param("year") Integer year, @Param("month") Integer month, @Param("limit") Integer limit);
    
    /**
     * 获取特定比赛的排行榜
     * @param competitionId 比赛ID
     * @return 排行榜列表
     */
    List<RankingDTO> getRankingByCompetition(@Param("competitionId") Integer competitionId);
    
    /**
     * 获取特定时间范围内的排行榜
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param limit 返回条数
     * @return 排行榜列表
     */
    List<RankingDTO> getRankingByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("limit") Integer limit);
    
    /**
     * 获取运动员的排名信息
     * @param athleteId 运动员ID
     * @return 排名信息
     */
    RankingDTO getAthleteRanking(@Param("athleteId") Long athleteId);
    
    /**
     * 获取运动员的月度排名
     * @param athleteId 运动员ID
     * @param year 年份
     * @param month 月份
     * @return 排名信息
     */
    RankingDTO getAthleteMonthlyRanking(@Param("athleteId") Long athleteId, @Param("year") Integer year, @Param("month") Integer month);
}
