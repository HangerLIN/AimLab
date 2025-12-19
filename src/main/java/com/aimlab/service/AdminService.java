package com.aimlab.service;

import com.aimlab.entity.Athlete;
import com.aimlab.entity.Competition;
import com.aimlab.entity.User;
import com.aimlab.mapper.AthleteMapper;
import com.aimlab.mapper.CompetitionMapper;
import com.aimlab.mapper.ShootingRecordMapper;
import com.aimlab.mapper.TrainingSessionMapper;
import com.aimlab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员后台服务
 */
@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AthleteMapper athleteMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private TrainingSessionMapper trainingSessionMapper;
    
    @Autowired
    private ShootingRecordMapper shootingRecordMapper;

    /**
     * 获取仪表盘概览数据
     *
     * @return 统计信息
     */
    public Map<String, Object> getDashboardMetrics() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 总览数据
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalUsers", userMapper.countAll());
        overview.put("totalAthletes", athleteMapper.countAll());
        overview.put("activeTrainings", trainingSessionMapper.countActive());
        overview.put("totalTrainings", trainingSessionMapper.countAll());
        overview.put("activeCompetitions",
                competitionMapper.countByStatus("RUNNING") + competitionMapper.countByStatus("PAUSED"));
        overview.put("totalCompetitions", competitionMapper.countAll());
        overview.put("recentReports", trainingSessionMapper.countReportsSince(sevenDaysAgo));
        
        // 计算平均成绩
        BigDecimal avgScore = shootingRecordMapper.getAverageScore();
        overview.put("avgScore", avgScore != null ? avgScore : BigDecimal.ZERO);

        // 用户统计
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("totalUsers", userMapper.countAll());
        userStats.put("activeUsers", userMapper.countByStatus(1));
        userStats.put("disabledUsers", userMapper.countByStatus(0));
        userStats.put("adminUsers", userMapper.countByRole("ADMIN"));

        // 待审批运动员
        long pendingAthleteTotal = athleteMapper.countByApprovalStatus("PENDING");
        List<Map<String, Object>> pendingAthleteItems = athleteMapper.findByApprovalStatus("PENDING", 5).stream()
                .map(this::toAthleteSummary)
                .collect(Collectors.toList());
        Map<String, Object> pendingAthletes = new HashMap<>();
        pendingAthletes.put("total", pendingAthleteTotal);
        pendingAthletes.put("items", pendingAthleteItems);

        // 即将开始的比赛
        long upcomingCompetitionTotal = competitionMapper.countByStatus("CREATED");
        List<Map<String, Object>> upcomingCompetitionItems = competitionMapper.findUpcoming(5).stream()
                .map(this::toCompetitionSummary)
                .collect(Collectors.toList());
        Map<String, Object> upcomingCompetitions = new HashMap<>();
        upcomingCompetitions.put("total", upcomingCompetitionTotal);
        upcomingCompetitions.put("items", upcomingCompetitionItems);

        Map<String, Object> todos = new HashMap<>();
        todos.put("pendingAthletes", pendingAthletes);
        todos.put("upcomingCompetitions", upcomingCompetitions);
        
        // 运动员级别分布
        Map<String, Long> athleteLevelDistribution = new HashMap<>();
        List<Athlete> allAthletes = athleteMapper.findAll();
        for (Athlete athlete : allAthletes) {
            String level = athlete.getLevel() != null ? athlete.getLevel() : "未知";
            athleteLevelDistribution.merge(level, 1L, Long::sum);
        }
        
        // 运动员排行（按训练次数和平均成绩）
        List<Map<String, Object>> athleteRanking = new ArrayList<>();
        try {
            List<Map<String, Object>> stats = shootingRecordMapper.getAthleteTrainingStats();
            if (stats != null && !stats.isEmpty()) {
                for (Map<String, Object> stat : stats) {
                    Map<String, Object> ranking = new HashMap<>();
                    ranking.put("name", stat.get("name"));
                    ranking.put("level", stat.get("level"));
                    ranking.put("trainingCount", stat.get("trainingCount"));
                    Object avgScoreVal = stat.get("avgScore");
                    ranking.put("avgScore", avgScoreVal != null ? ((Number) avgScoreVal).doubleValue() : 0.0);
                    athleteRanking.add(ranking);
                }
            }
        } catch (Exception e) {
            // 如果统计失败，使用默认空列表
            e.printStackTrace();
        }
        
        // 如果没有训练统计数据，使用运动员基本信息
        if (athleteRanking.isEmpty()) {
            for (Athlete athlete : allAthletes.stream().limit(10).collect(Collectors.toList())) {
                Map<String, Object> ranking = new HashMap<>();
                ranking.put("name", athlete.getName());
                ranking.put("level", athlete.getLevel());
                ranking.put("trainingCount", 0);
                ranking.put("avgScore", 0.0);
                athleteRanking.add(ranking);
            }
        }
        
        // 成绩分布统计
        List<Map<String, Object>> scoreDistribution = new ArrayList<>();
        try {
            scoreDistribution = shootingRecordMapper.getScoreDistribution();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 比赛状态统计
        Map<String, Long> competitionStatusCounts = new HashMap<>();
        competitionStatusCounts.put("CREATED", competitionMapper.countByStatus("CREATED"));
        competitionStatusCounts.put("RUNNING", competitionMapper.countByStatus("RUNNING"));
        competitionStatusCounts.put("PAUSED", competitionMapper.countByStatus("PAUSED"));
        competitionStatusCounts.put("COMPLETED", competitionMapper.countByStatus("COMPLETED"));
        competitionStatusCounts.put("CANCELLED", competitionMapper.countByStatus("CANCELLED"));
        
        // 最近比赛
        List<Map<String, Object>> recentCompetitions = competitionMapper.findAll().stream()
                .limit(5)
                .map(comp -> {
                    Map<String, Object> item = toCompetitionSummary(comp);
                    item.put("project", "射击");
                    item.put("participantCount", 0);
                    item.put("date", comp.getCreatedAt());
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("overview", overview);
        metrics.put("userStats", userStats);
        metrics.put("todos", todos);
        metrics.put("athleteLevelDistribution", athleteLevelDistribution);
        metrics.put("athleteRanking", athleteRanking);
        metrics.put("recentCompetitions", recentCompetitions);
        metrics.put("scoreDistribution", scoreDistribution);
        metrics.put("competitionStatusCounts", competitionStatusCounts);
        return metrics;
    }

    /**
     * 获取全部用户（脱敏）
     *
     * @return 用户列表
     */
    public List<Map<String, Object>> listUsers() {
        return userMapper.findAll().stream()
                .map(this::toUserSummary)
                .collect(Collectors.toList());
    }

    /**
     * 获取全部运动员
     *
     * @return 运动员列表
     */
    public List<Map<String, Object>> listAthletes() {
        return athleteMapper.findAll().stream()
                .map(this::toAthleteSummary)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toUserSummary(User user) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", user.getId());
        summary.put("username", user.getUsername());
        summary.put("role", user.getRole());
        summary.put("status", user.getStatus());
        summary.put("createdAt", user.getCreatedAt());
        summary.put("updatedAt", user.getUpdatedAt());
        return summary;
    }

    private Map<String, Object> toAthleteSummary(Athlete athlete) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", athlete.getId());
        summary.put("userId", athlete.getUserId());
        summary.put("name", athlete.getName());
        summary.put("gender", athlete.getGender());
        summary.put("level", athlete.getLevel());
        summary.put("birthDate", athlete.getBirthDate());
        summary.put("approvalStatus", athlete.getApprovalStatus());
        summary.put("modificationStatus", athlete.getModificationStatus());
        summary.put("pendingModificationData", athlete.getPendingModificationData());
        summary.put("createdAt", athlete.getCreatedAt());
        summary.put("updatedAt", athlete.getUpdatedAt());
        return summary;
    }

    private Map<String, Object> toCompetitionSummary(Competition competition) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", competition.getId());
        summary.put("name", competition.getName());
        summary.put("status", competition.getStatus());
        summary.put("roundsCount", competition.getRoundsCount());
        summary.put("shotsPerRound", competition.getShotsPerRound());
        summary.put("createdAt", competition.getCreatedAt());
        summary.put("startedAt", competition.getStartedAt());
        summary.put("completedAt", competition.getCompletedAt());
        return summary;
    }
}
