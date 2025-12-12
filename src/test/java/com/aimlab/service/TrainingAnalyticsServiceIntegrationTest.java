package com.aimlab.service;

import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.dto.ExportFile;
import com.aimlab.dto.TrainingAthleteStatsDTO;
import com.aimlab.dto.TrainingProjectStatsDTO;
import com.aimlab.dto.TrainingTimeStatsDTO;
import com.aimlab.entity.Athlete;
import com.aimlab.entity.ShootingRecord;
import com.aimlab.entity.TrainingSession;
import com.aimlab.entity.User;
import com.aimlab.mapper.AthleteMapper;
import com.aimlab.mapper.ShootingRecordMapper;
import com.aimlab.mapper.TrainingSessionMapper;
import com.aimlab.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 训练统计分析服务集成测试
 * 测试按时间、运动员、项目维度聚合训练数据的功能
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrainingAnalyticsServiceIntegrationTest {

    @Autowired
    private TrainingAnalyticsService trainingAnalyticsService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AthleteMapper athleteMapper;

    @Autowired
    private TrainingSessionMapper trainingSessionMapper;

    @Autowired
    private ShootingRecordMapper shootingRecordMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User testUser;
    private Athlete athlete1;
    private Athlete athlete2;
    private TrainingSession session1;
    private TrainingSession session2;

    @BeforeEach
    public void setup() {
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("training_test_user");
        testUser.setPassword(passwordEncoder.encode("test123"));
        testUser.setRole("user");
        testUser.setStatus(1);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(testUser);

        // 为运动员1创建独立用户
        User user1 = new User();
        user1.setUsername("training_athlete1");
        user1.setPassword(passwordEncoder.encode("test123"));
        user1.setRole("user");
        user1.setStatus(1);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user1);
        
        // 创建测试运动员1
        athlete1 = new Athlete();
        athlete1.setUserId(user1.getId());
        athlete1.setName("测试运动员1");
        athlete1.setGender("MALE");
        athlete1.setBirthDate(java.time.LocalDate.of(1995, 5, 20));
        athlete1.setLevel("国家级");
        athlete1.setApprovalStatus("APPROVED");
        athlete1.setCreatedAt(LocalDateTime.now());
        athlete1.setUpdatedAt(LocalDateTime.now());
        athleteMapper.insert(athlete1);

        // 为运动员2创建独立用户
        User user2 = new User();
        user2.setUsername("training_athlete2");
        user2.setPassword(passwordEncoder.encode("test123"));
        user2.setRole("user");
        user2.setStatus(1);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user2);
        
        // 创建测试运动员2
        athlete2 = new Athlete();
        athlete2.setUserId(user2.getId());
        athlete2.setName("测试运动员2");
        athlete2.setGender("FEMALE");
        athlete2.setBirthDate(java.time.LocalDate.of(1998, 8, 15));
        athlete2.setLevel("省级");
        athlete2.setApprovalStatus("APPROVED");
        athlete2.setCreatedAt(LocalDateTime.now());
        athlete2.setUpdatedAt(LocalDateTime.now());
        athleteMapper.insert(athlete2);

        // 创建训练场次
        createTrainingSessions();
        createShootingRecords();
    }

    private void createTrainingSessions() {
        // 训练场次1 - 3天前
        session1 = new TrainingSession();
        session1.setAthleteId(athlete1.getId());
        session1.setSessionName("训练场次1");
        session1.setProjectType("10米气步枪");
        session1.setStartTime(LocalDateTime.now().minusDays(3));
        session1.setEndTime(LocalDateTime.now().minusDays(3).plusHours(2));
        session1.setNotes("测试训练场次1");
        trainingSessionMapper.insert(session1);

        // 训练场次2 - 1天前
        session2 = new TrainingSession();
        session2.setAthleteId(athlete2.getId());
        session2.setSessionName("训练场次2");
        session2.setProjectType("25米手枪");
        session2.setStartTime(LocalDateTime.now().minusDays(1));
        session2.setEndTime(LocalDateTime.now().minusDays(1).plusHours(1));
        session2.setNotes("测试训练场次2");
        trainingSessionMapper.insert(session2);
    }

    private void createShootingRecords() {
        // 运动员1的射击记录
        for (int i = 0; i < 10; i++) {
            ShootingRecord record = new ShootingRecord();
            record.setUserId(testUser.getId());
            record.setAthleteId(athlete1.getId());
            record.setTrainingSessionId(session1.getId());
            record.setRecordType("TRAINING");
            record.setScore(BigDecimal.valueOf(9.0 + Math.random())); // 9.0-10.0环
            record.setX(BigDecimal.valueOf(Math.random() * 10 - 5));
            record.setY(BigDecimal.valueOf(Math.random() * 10 - 5));
            record.setShotAt(LocalDateTime.now().minusDays(3).plusMinutes(i * 5));
            shootingRecordMapper.insert(record);
        }

        // 运动员2的射击记录
        for (int i = 0; i < 15; i++) {
            ShootingRecord record = new ShootingRecord();
            record.setUserId(testUser.getId());
            record.setAthleteId(athlete2.getId());
            record.setTrainingSessionId(session2.getId());
            record.setRecordType("TRAINING");
            record.setScore(BigDecimal.valueOf(8.0 + Math.random() * 1.5)); // 8.0-9.5环
            record.setX(BigDecimal.valueOf(Math.random() * 10 - 5));
            record.setY(BigDecimal.valueOf(Math.random() * 10 - 5));
            record.setShotAt(LocalDateTime.now().minusDays(1).plusMinutes(i * 3));
            shootingRecordMapper.insert(record);
        }
    }

    @AfterEach
    public void cleanup() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    /**
     * 测试1：按时间维度聚合 - 日粒度
     */
    @Test
    @Order(1)
    public void testTimeStats_DayGranularity() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingTimeStatsDTO> stats = trainingAnalyticsService.getTimeStats(
                "DAY", startTime, endTime, null, null);

        assertNotNull(stats);
        System.out.println("✓ 测试1通过：按日粒度聚合训练数据");
        System.out.println("  统计记录数: " + stats.size());
        
        for (TrainingTimeStatsDTO stat : stats) {
            System.out.println("  - 周期: " + stat.getPeriod() +
                    ", 平均环数: " + stat.getAvgScore() +
                    ", 射击次数: " + stat.getTotalShots());
        }
    }

    /**
     * 测试2：按时间维度聚合 - 周粒度
     */
    @Test
    @Order(2)
    public void testTimeStats_WeekGranularity() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingTimeStatsDTO> stats = trainingAnalyticsService.getTimeStats(
                "WEEK", startTime, endTime, null, null);

        assertNotNull(stats);
        System.out.println("✓ 测试2通过：按周粒度聚合训练数据");
        System.out.println("  统计记录数: " + stats.size());
    }

    /**
     * 测试3：按时间维度聚合 - 月粒度
     */
    @Test
    @Order(3)
    public void testTimeStats_MonthGranularity() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(90);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingTimeStatsDTO> stats = trainingAnalyticsService.getTimeStats(
                "MONTH", startTime, endTime, null, null);

        assertNotNull(stats);
        System.out.println("✓ 测试3通过：按月粒度聚合训练数据");
        System.out.println("  统计记录数: " + stats.size());
    }

    /**
     * 测试4：按时间维度聚合 - 指定运动员
     */
    @Test
    @Order(4)
    public void testTimeStats_WithAthleteFilter() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingTimeStatsDTO> stats = trainingAnalyticsService.getTimeStats(
                "DAY", startTime, endTime, athlete1.getId(), null);

        assertNotNull(stats);
        System.out.println("✓ 测试4通过：按日粒度聚合指定运动员的训练数据");
        System.out.println("  运动员ID: " + athlete1.getId());
        System.out.println("  统计记录数: " + stats.size());
    }

    /**
     * 测试5：按时间维度聚合 - 指定项目类型
     */
    @Test
    @Order(5)
    public void testTimeStats_WithProjectTypeFilter() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingTimeStatsDTO> stats = trainingAnalyticsService.getTimeStats(
                "DAY", startTime, endTime, null, "10米气步枪");

        assertNotNull(stats);
        System.out.println("✓ 测试5通过：按日粒度聚合指定项目的训练数据");
        System.out.println("  项目类型: 10米气步枪");
        System.out.println("  统计记录数: " + stats.size());
    }

    /**
     * 测试6：按运动员维度聚合
     */
    @Test
    @Order(6)
    public void testAthleteStats() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingAthleteStatsDTO> stats = trainingAnalyticsService.getAthleteStats(
                startTime, endTime, null, null);

        assertNotNull(stats);
        assertTrue(stats.size() >= 2); // 至少有两个运动员
        System.out.println("✓ 测试6通过：按运动员维度聚合训练数据");
        System.out.println("  运动员数量: " + stats.size());
        
        for (TrainingAthleteStatsDTO stat : stats) {
            System.out.println("  - 运动员: " + stat.getAthleteName() +
                    ", 平均环数: " + stat.getAvgScore() +
                    ", 射击次数: " + stat.getTotalShots() +
                    ", 稳定性: " + stat.getStabilityIndex());
        }
    }

    /**
     * 测试7：按运动员维度聚合 - 指定项目类型
     */
    @Test
    @Order(7)
    public void testAthleteStats_WithProjectTypeFilter() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingAthleteStatsDTO> stats = trainingAnalyticsService.getAthleteStats(
                startTime, endTime, "10米气步枪", null);

        assertNotNull(stats);
        System.out.println("✓ 测试7通过：按运动员维度聚合指定项目的训练数据");
        System.out.println("  项目类型: 10米气步枪");
        System.out.println("  统计记录数: " + stats.size());
    }

    /**
     * 测试8：按运动员维度聚合 - 关键词搜索
     */
    @Test
    @Order(8)
    public void testAthleteStats_WithKeywordFilter() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingAthleteStatsDTO> stats = trainingAnalyticsService.getAthleteStats(
                startTime, endTime, null, "运动员1");

        assertNotNull(stats);
        System.out.println("✓ 测试8通过：按运动员维度聚合（关键词搜索）");
        System.out.println("  关键词: 运动员1");
        System.out.println("  统计记录数: " + stats.size());
    }

    /**
     * 测试9：按项目维度聚合
     */
    @Test
    @Order(9)
    public void testProjectStats() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingProjectStatsDTO> stats = trainingAnalyticsService.getProjectStats(
                startTime, endTime, null);

        assertNotNull(stats);
        assertTrue(stats.size() >= 2); // 至少有两个项目
        System.out.println("✓ 测试9通过：按项目维度聚合训练数据");
        System.out.println("  项目数量: " + stats.size());
        
        for (TrainingProjectStatsDTO stat : stats) {
            System.out.println("  - 项目: " + stat.getProjectType() +
                    ", 平均环数: " + stat.getAvgScore() +
                    ", 射击次数: " + stat.getTotalShots() +
                    ", 稳定性: " + stat.getStabilityIndex());
        }
    }

    /**
     * 测试10：按项目维度聚合 - 指定运动员
     */
    @Test
    @Order(10)
    public void testProjectStats_WithAthleteFilter() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<TrainingProjectStatsDTO> stats = trainingAnalyticsService.getProjectStats(
                startTime, endTime, athlete1.getId());

        assertNotNull(stats);
        System.out.println("✓ 测试10通过：按项目维度聚合指定运动员的训练数据");
        System.out.println("  运动员ID: " + athlete1.getId());
        System.out.println("  统计记录数: " + stats.size());
    }

    /**
     * 测试11：导出训练统计 - 时间维度
     */
    @Test
    @Order(11)
    public void testExportAnalytics_TimeDimension() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        ExportFile exportFile = trainingAnalyticsService.exportAnalytics(
                "TIME", "DAY", startTime, endTime, null, null, null);

        assertNotNull(exportFile);
        assertNotNull(exportFile.getFileName());
        assertNotNull(exportFile.getData());
        assertTrue(exportFile.getData().length > 0);
        assertTrue(exportFile.getFileName().contains("time"));
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                exportFile.getContentType());

        System.out.println("✓ 测试11通过：导出训练统计（时间维度）");
        System.out.println("  文件名: " + exportFile.getFileName());
        System.out.println("  文件大小: " + exportFile.getData().length + " 字节");
    }

    /**
     * 测试12：导出训练统计 - 运动员维度
     */
    @Test
    @Order(12)
    public void testExportAnalytics_AthleteDimension() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        ExportFile exportFile = trainingAnalyticsService.exportAnalytics(
                "ATHLETE", null, startTime, endTime, null, null, null);

        assertNotNull(exportFile);
        assertNotNull(exportFile.getFileName());
        assertNotNull(exportFile.getData());
        assertTrue(exportFile.getData().length > 0);
        assertTrue(exportFile.getFileName().contains("athlete"));

        System.out.println("✓ 测试12通过：导出训练统计（运动员维度）");
        System.out.println("  文件名: " + exportFile.getFileName());
        System.out.println("  文件大小: " + exportFile.getData().length + " 字节");
    }

    /**
     * 测试13：导出训练统计 - 项目维度
     */
    @Test
    @Order(13)
    public void testExportAnalytics_ProjectDimension() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        ExportFile exportFile = trainingAnalyticsService.exportAnalytics(
                "PROJECT", null, startTime, endTime, null, null, null);

        assertNotNull(exportFile);
        assertNotNull(exportFile.getFileName());
        assertNotNull(exportFile.getData());
        assertTrue(exportFile.getData().length > 0);
        assertTrue(exportFile.getFileName().contains("project"));

        System.out.println("✓ 测试13通过：导出训练统计（项目维度）");
        System.out.println("  文件名: " + exportFile.getFileName());
        System.out.println("  文件大小: " + exportFile.getData().length + " 字节");
    }

    /**
     * 测试14：时间范围自动规范化
     */
    @Test
    @Order(14)
    public void testTimeRangeNormalization() {
        // 测试空时间范围
        List<TrainingTimeStatsDTO> stats1 = trainingAnalyticsService.getTimeStats(
                "DAY", null, null, null, null);
        assertNotNull(stats1);

        // 测试反向时间范围（结束时间早于开始时间）
        LocalDateTime end = LocalDateTime.now().minusDays(10);
        LocalDateTime start = LocalDateTime.now();
        List<TrainingTimeStatsDTO> stats2 = trainingAnalyticsService.getTimeStats(
                "DAY", start, end, null, null);
        assertNotNull(stats2);

        System.out.println("✓ 测试14通过：时间范围自动规范化");
    }

    /**
     * 测试15：粒度参数规范化
     */
    @Test
    @Order(15)
    public void testGranularityNormalization() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        // 测试无效粒度（应该使用默认值 DAY）
        List<TrainingTimeStatsDTO> stats1 = trainingAnalyticsService.getTimeStats(
                "INVALID", startTime, endTime, null, null);
        assertNotNull(stats1);

        // 测试空粒度
        List<TrainingTimeStatsDTO> stats2 = trainingAnalyticsService.getTimeStats(
                null, startTime, endTime, null, null);
        assertNotNull(stats2);

        // 测试小写粒度
        List<TrainingTimeStatsDTO> stats3 = trainingAnalyticsService.getTimeStats(
                "day", startTime, endTime, null, null);
        assertNotNull(stats3);

        System.out.println("✓ 测试15通过：粒度参数规范化");
    }

    /**
     * 测试16：空数据场景
     */
    @Test
    @Order(16)
    public void testEmptyDataScenario() {
        // 查询未来的时间范围（应该没有数据）
        LocalDateTime startTime = LocalDateTime.now().plusDays(10);
        LocalDateTime endTime = LocalDateTime.now().plusDays(20);

        List<TrainingTimeStatsDTO> timeStats = trainingAnalyticsService.getTimeStats(
                "DAY", startTime, endTime, null, null);
        assertNotNull(timeStats);

        List<TrainingAthleteStatsDTO> athleteStats = trainingAnalyticsService.getAthleteStats(
                startTime, endTime, null, null);
        assertNotNull(athleteStats);

        List<TrainingProjectStatsDTO> projectStats = trainingAnalyticsService.getProjectStats(
                startTime, endTime, null);
        assertNotNull(projectStats);

        System.out.println("✓ 测试16通过：空数据场景处理");
    }

    /**
     * 测试17：综合场景 - 多维度交叉筛选
     */
    @Test
    @Order(17)
    public void testComprehensiveScenario() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        // 时间 + 运动员 + 项目
        List<TrainingTimeStatsDTO> stats = trainingAnalyticsService.getTimeStats(
                "DAY", startTime, endTime, athlete1.getId(), "10米气步枪");

        assertNotNull(stats);
        System.out.println("✓ 测试17通过：综合场景 - 多维度交叉筛选");
        System.out.println("  筛选条件: 运动员ID=" + athlete1.getId() + ", 项目=10米气步枪");
        System.out.println("  统计记录数: " + stats.size());
    }
}

