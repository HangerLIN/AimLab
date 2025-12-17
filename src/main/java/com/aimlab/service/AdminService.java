package com.aimlab.service;

import com.aimlab.entity.Athlete;
import com.aimlab.entity.Competition;
import com.aimlab.entity.User;
import com.aimlab.mapper.AthleteMapper;
import com.aimlab.mapper.CompetitionMapper;
import com.aimlab.mapper.TrainingSessionMapper;
import com.aimlab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    /**
     * 获取仪表盘概览数据
     *
     * @return 统计信息
     */
    public Map<String, Object> getDashboardMetrics() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalUsers", userMapper.countAll());
        overview.put("activeTrainings", trainingSessionMapper.countActive());
        overview.put("activeCompetitions",
                competitionMapper.countByStatus("RUNNING") + competitionMapper.countByStatus("PAUSED"));
        overview.put("recentReports", trainingSessionMapper.countReportsSince(sevenDaysAgo));

        // 用户统计
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("totalUsers", userMapper.countAll());
        userStats.put("activeUsers", userMapper.countByStatus(1));
        userStats.put("disabledUsers", userMapper.countByStatus(0));
        userStats.put("adminUsers", userMapper.countByRole("ADMIN"));

        long pendingAthleteTotal = athleteMapper.countByApprovalStatus("PENDING");
        List<Map<String, Object>> pendingAthleteItems = athleteMapper.findByApprovalStatus("PENDING", 5).stream()
                .map(this::toAthleteSummary)
                .collect(Collectors.toList());
        Map<String, Object> pendingAthletes = new HashMap<>();
        pendingAthletes.put("total", pendingAthleteTotal);
        pendingAthletes.put("items", pendingAthleteItems);

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

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("overview", overview);
        metrics.put("userStats", userStats);
        metrics.put("todos", todos);
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
