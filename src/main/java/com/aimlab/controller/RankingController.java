package com.aimlab.controller;

import com.aimlab.dto.RankingDTO;
import com.aimlab.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 排行榜接口控制器
 */
@RestController
@RequestMapping("/api/rankings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RankingController {
    
    @Autowired
    private RankingService rankingService;
    
    /**
     * 获取全站排行榜
     * GET /api/rankings/overall?limit=100
     */
    @GetMapping("/overall")
    public List<RankingDTO> getOverallRanking(
            @RequestParam(value = "limit", defaultValue = "100") Integer limit) {
        return rankingService.getOverallRanking(limit);
    }
    
    /**
     * 获取分类排行榜（按等级分类）
     * GET /api/rankings/by-level?level=国家级&limit=50
     */
    @GetMapping("/by-level")
    public List<RankingDTO> getRankingByLevel(
            @RequestParam(value = "level") String level,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        return rankingService.getRankingByLevel(level, limit);
    }
    
    /**
     * 获取月度排行榜
     * GET /api/rankings/monthly?year=2025&month=12&limit=50
     */
    @GetMapping("/monthly")
    public List<RankingDTO> getMonthlyRanking(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        if (year == null || month == null) {
            return rankingService.getCurrentMonthRanking(limit);
        }
        return rankingService.getMonthlyRanking(year, month, limit);
    }
    
    /**
     * 获取特定比赛的排行榜
     * GET /api/rankings/competition/64
     */
    @GetMapping("/competition/{competitionId}")
    public List<RankingDTO> getRankingByCompetition(
            @PathVariable Integer competitionId) {
        return rankingService.getRankingByCompetition(competitionId);
    }
    
    /**
     * 获取特定时间范围内的排行榜
     * GET /api/rankings/date-range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59&limit=50
     */
    @GetMapping("/date-range")
    public List<RankingDTO> getRankingByDateRange(
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        return rankingService.getRankingByDateRange(start, end, limit);
    }
    
    /**
     * 获取运动员的排名信息
     * GET /api/rankings/athlete/1
     */
    @GetMapping("/athlete/{athleteId}")
    public RankingDTO getAthleteRanking(@PathVariable Long athleteId) {
        return rankingService.getAthleteRanking(athleteId);
    }
    
    /**
     * 获取运动员的月度排名
     * GET /api/rankings/athlete/1/monthly?year=2025&month=12
     */
    @GetMapping("/athlete/{athleteId}/monthly")
    public RankingDTO getAthleteMonthlyRanking(
            @PathVariable Long athleteId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        if (year == null || month == null) {
            return rankingService.getAthleteCurrentMonthRanking(athleteId);
        }
        return rankingService.getAthleteMonthlyRanking(athleteId, year, month);
    }
}
