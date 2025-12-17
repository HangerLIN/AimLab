package com.aimlab.service;

import com.aimlab.dto.RankingDTO;
import com.aimlab.mapper.RankingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * 排行榜服务类
 */
@Service
public class RankingService {
    
    @Autowired
    private RankingMapper rankingMapper;
    
    /**
     * 获取全站排行榜
     * @param limit 返回条数
     * @return 排行榜列表
     */
    public List<RankingDTO> getOverallRanking(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        return rankingMapper.getOverallRanking(limit);
    }
    
    /**
     * 获取分类排行榜（按等级分类）
     * @param level 运动员等级
     * @param limit 返回条数
     * @return 排行榜列表
     */
    public List<RankingDTO> getRankingByLevel(String level, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        return rankingMapper.getRankingByLevel(level, limit);
    }
    
    /**
     * 获取月度排行榜
     * @param year 年份
     * @param month 月份
     * @param limit 返回条数
     * @return 排行榜列表
     */
    public List<RankingDTO> getMonthlyRanking(Integer year, Integer month, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        return rankingMapper.getMonthlyRanking(year, month, limit);
    }
    
    /**
     * 获取当前月份的排行榜
     * @param limit 返回条数
     * @return 排行榜列表
     */
    public List<RankingDTO> getCurrentMonthRanking(Integer limit) {
        YearMonth now = YearMonth.now();
        return getMonthlyRanking(now.getYear(), now.getMonthValue(), limit);
    }
    
    /**
     * 获取特定比赛的排行榜
     * @param competitionId 比赛ID
     * @return 排行榜列表
     */
    public List<RankingDTO> getRankingByCompetition(Integer competitionId) {
        return rankingMapper.getRankingByCompetition(competitionId);
    }
    
    /**
     * 获取特定时间范围内的排行榜
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param limit 返回条数
     * @return 排行榜列表
     */
    public List<RankingDTO> getRankingByDateRange(LocalDateTime startDate, LocalDateTime endDate, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        return rankingMapper.getRankingByDateRange(startDate, endDate, limit);
    }
    
    /**
     * 获取运动员的排名信息
     * @param athleteId 运动员ID
     * @return 排名信息
     */
    public RankingDTO getAthleteRanking(Long athleteId) {
        return rankingMapper.getAthleteRanking(athleteId);
    }
    
    /**
     * 获取运动员的月度排名
     * @param athleteId 运动员ID
     * @param year 年份
     * @param month 月份
     * @return 排名信息
     */
    public RankingDTO getAthleteMonthlyRanking(Long athleteId, Integer year, Integer month) {
        return rankingMapper.getAthleteMonthlyRanking(athleteId, year, month);
    }
    
    /**
     * 获取运动员当前月度的排名
     * @param athleteId 运动员ID
     * @return 排名信息
     */
    public RankingDTO getAthleteCurrentMonthRanking(Long athleteId) {
        YearMonth now = YearMonth.now();
        return getAthleteMonthlyRanking(athleteId, now.getYear(), now.getMonthValue());
    }
}
