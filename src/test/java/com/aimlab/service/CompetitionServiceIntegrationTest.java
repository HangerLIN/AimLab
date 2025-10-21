package com.aimlab.service;

import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.dto.ExportFile;
import com.aimlab.entity.Athlete;
import com.aimlab.entity.Competition;
import com.aimlab.entity.CompetitionAthlete;
import com.aimlab.entity.CompetitionResult;
import com.aimlab.entity.User;
import com.aimlab.mapper.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 比赛服务集成测试
 * 测试比赛管理新增功能：强制结束、报名权限、导出成绩等
 */
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CompetitionServiceIntegrationTest {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AthleteMapper athleteMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private CompetitionResultMapper competitionResultMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User adminUser;
    private User normalUser;
    private Athlete athlete1;
    private Athlete athlete2;
    private Competition testCompetition;

    @BeforeEach
    public void setup() {
        // 创建管理员用户
        adminUser = new User();
        adminUser.setUsername("competition_admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole("admin");
        adminUser.setStatus(1);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(adminUser);

        // 创建普通用户
        normalUser = new User();
        normalUser.setUsername("competition_user");
        normalUser.setPassword(passwordEncoder.encode("user123"));
        normalUser.setRole("user");
        normalUser.setStatus(1);
        normalUser.setCreatedAt(LocalDateTime.now());
        normalUser.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(normalUser);

        // 创建测试运动员
        athlete1 = new Athlete();
        athlete1.setUserId(normalUser.getId());
        athlete1.setName("比赛运动员1");
        athlete1.setGender("MALE");
        athlete1.setBirthDate(java.time.LocalDate.of(1995, 3, 15));
        athlete1.setLevel("国家级");
        athlete1.setApprovalStatus("APPROVED");
        athlete1.setCreatedAt(LocalDateTime.now());
        athlete1.setUpdatedAt(LocalDateTime.now());
        athleteMapper.insert(athlete1);

        athlete2 = new Athlete();
        athlete2.setUserId(normalUser.getId());
        athlete2.setName("比赛运动员2");
        athlete2.setGender("FEMALE");
        athlete2.setBirthDate(java.time.LocalDate.of(1997, 7, 20));
        athlete2.setLevel("省级");
        athlete2.setApprovalStatus("APPROVED");
        athlete2.setCreatedAt(LocalDateTime.now());
        athlete2.setUpdatedAt(LocalDateTime.now());
        athleteMapper.insert(athlete2);

        // 创建测试比赛
        createTestCompetition();
    }

    private void createTestCompetition() {
        testCompetition = new Competition();
        testCompetition.setName("测试比赛");
        testCompetition.setDescription("用于集成测试的比赛");
        testCompetition.setFormatType("STANDARD");
        testCompetition.setRoundsCount(3);
        testCompetition.setShotsPerRound(10);
        testCompetition.setTimeLimitPerShot(60);
        testCompetition.setStatus("CREATED");
        testCompetition.setAccessLevel("PUBLIC");
        testCompetition.setEnrollStartAt(LocalDateTime.now().minusDays(1));
        testCompetition.setEnrollEndAt(LocalDateTime.now().plusDays(1));
        testCompetition.setCreatedBy(adminUser.getId());
        testCompetition.setCreatedAt(LocalDateTime.now());
        competitionMapper.insert(testCompetition);
    }

    @AfterEach
    public void cleanup() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    /**
     * 测试1：创建比赛 - 基本配置
     */
    @Test
    @Order(1)
    public void testCreateCompetition_BasicConfig() {
        StpUtil.login(adminUser.getId());

        Competition competition = new Competition();
        competition.setName("新建测试比赛");
        competition.setDescription("测试描述");
        competition.setFormatType("STANDARD");
        competition.setRoundsCount(5);
        competition.setShotsPerRound(10);
        competition.setTimeLimitPerShot(60);
        competition.setAccessLevel("PUBLIC");
        competition.setEnrollStartAt(LocalDateTime.now());
        competition.setEnrollEndAt(LocalDateTime.now().plusDays(7));

        Competition created = competitionService.createCompetition(competition);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("CREATED", created.getStatus());
        assertEquals("PUBLIC", created.getAccessLevel());
        assertEquals("STANDARD", created.getFormatType());
        assertNotNull(created.getCreatedAt());

        System.out.println("✓ 测试1通过：创建比赛成功");
        System.out.println("  比赛ID: " + created.getId());
        System.out.println("  比赛名称: " + created.getName());
    }

    /**
     * 测试2：创建比赛 - ADMIN_ONLY访问级别
     */
    @Test
    @Order(2)
    public void testCreateCompetition_AdminOnlyAccess() {
        StpUtil.login(adminUser.getId());

        Competition competition = new Competition();
        competition.setName("管理员专属比赛");
        competition.setAccessLevel("ADMIN_ONLY");
        competition.setRoundsCount(3);
        competition.setShotsPerRound(10);
        competition.setEnrollStartAt(LocalDateTime.now());
        competition.setEnrollEndAt(LocalDateTime.now().plusDays(7));

        Competition created = competitionService.createCompetition(competition);

        assertNotNull(created);
        assertEquals("ADMIN_ONLY", created.getAccessLevel());

        System.out.println("✓ 测试2通过：创建管理员专属比赛");
    }

    /**
     * 测试3：更新比赛配置 - 修改赛制和报名窗口
     */
    @Test
    @Order(3)
    public void testUpdateCompetition() {
        Competition update = new Competition();
        update.setId(testCompetition.getId());
        update.setName("更新后的测试比赛");
        update.setRoundsCount(5);
        update.setShotsPerRound(15);
        update.setEnrollStartAt(LocalDateTime.now().minusHours(12));
        update.setEnrollEndAt(LocalDateTime.now().plusHours(12));
        update.setAccessLevel("ADMIN_ONLY");

        Competition updated = competitionService.updateCompetition(update);

        assertNotNull(updated);
        assertEquals("更新后的测试比赛", updated.getName());
        assertEquals(5, updated.getRoundsCount());
        assertEquals(15, updated.getShotsPerRound());
        assertEquals("ADMIN_ONLY", updated.getAccessLevel());

        System.out.println("✓ 测试3通过：更新比赛配置成功");
    }

    /**
     * 测试4：报名权限 - PUBLIC模式普通用户报名
     */
    @Test
    @Order(4)
    public void testEnrollment_PublicAccess() {
        StpUtil.login(normalUser.getId());
        StpUtil.getSession().set("role", "user");

        // 确保比赛是公开报名
        Competition comp = competitionMapper.findById(testCompetition.getId());
        comp.setAccessLevel("PUBLIC");
        competitionMapper.update(comp);

        CompetitionAthlete enrollment = new CompetitionAthlete();
        enrollment.setCompetitionId(testCompetition.getId());
        enrollment.setAthleteId(athlete1.getId());

        CompetitionAthlete result = competitionService.registerAthlete(enrollment);

        assertNotNull(result);
        assertEquals(testCompetition.getId(), result.getCompetitionId());
        assertEquals(athlete1.getId(), result.getAthleteId());

        System.out.println("✓ 测试4通过：PUBLIC模式下普通用户成功报名");
    }

    /**
     * 测试5：报名权限 - ADMIN_ONLY模式普通用户被拒绝
     */
    @Test
    @Order(5)
    public void testEnrollment_AdminOnlyReject() {
        // 创建管理员专属比赛
        Competition adminOnlyComp = new Competition();
        adminOnlyComp.setName("管理员专属比赛测试");
        adminOnlyComp.setAccessLevel("ADMIN_ONLY");
        adminOnlyComp.setRoundsCount(3);
        adminOnlyComp.setShotsPerRound(10);
        adminOnlyComp.setEnrollStartAt(LocalDateTime.now());
        adminOnlyComp.setEnrollEndAt(LocalDateTime.now().plusDays(1));
        adminOnlyComp.setStatus("CREATED");
        adminOnlyComp.setCreatedAt(LocalDateTime.now());
        competitionMapper.insert(adminOnlyComp);

        StpUtil.login(normalUser.getId());
        StpUtil.getSession().set("role", "user");

        CompetitionAthlete enrollment = new CompetitionAthlete();
        enrollment.setCompetitionId(adminOnlyComp.getId());
        enrollment.setAthleteId(athlete1.getId());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            competitionService.registerAthlete(enrollment);
        });

        assertTrue(exception.getMessage().contains("仅限管理员"));
        System.out.println("✓ 测试5通过：ADMIN_ONLY模式下普通用户被正确拒绝");
    }

    /**
     * 测试6：报名时间窗口验证
     */
    @Test
    @Order(6)
    public void testEnrollment_TimeWindow() {
        // 创建报名时间已过的比赛
        Competition pastComp = new Competition();
        pastComp.setName("报名已截止比赛");
        pastComp.setAccessLevel("PUBLIC");
        pastComp.setRoundsCount(3);
        pastComp.setShotsPerRound(10);
        pastComp.setEnrollStartAt(LocalDateTime.now().minusDays(10));
        pastComp.setEnrollEndAt(LocalDateTime.now().minusDays(1)); // 已截止
        pastComp.setStatus("CREATED");
        pastComp.setCreatedAt(LocalDateTime.now());
        competitionMapper.insert(pastComp);

        StpUtil.login(normalUser.getId());
        StpUtil.getSession().set("role", "user");

        CompetitionAthlete enrollment = new CompetitionAthlete();
        enrollment.setCompetitionId(pastComp.getId());
        enrollment.setAthleteId(athlete1.getId());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            competitionService.registerAthlete(enrollment);
        });

        assertTrue(exception.getMessage().contains("报名时间"));
        System.out.println("✓ 测试6通过：报名时间窗口验证通过");
    }

    /**
     * 测试7：批量报名运动员
     */
    @Test
    @Order(7)
    public void testBatchEnrollment() {
        StpUtil.login(adminUser.getId());
        StpUtil.getSession().set("role", "admin");

        List<Long> athleteIds = Arrays.asList(athlete1.getId(), athlete2.getId());
        int enrolled = competitionService.enrollAthletes(testCompetition.getId(), athleteIds);

        assertTrue(enrolled > 0);
        System.out.println("✓ 测试7通过：批量报名成功，报名人数: " + enrolled);
    }

    /**
     * 测试8：开始比赛
     */
    @Test
    @Order(8)
    public void testStartCompetition() {
        Competition started = competitionService.startCompetition(testCompetition.getId());

        assertNotNull(started);
        assertEquals("RUNNING", started.getStatus());
        assertNotNull(started.getStartedAt());

        System.out.println("✓ 测试8通过：开始比赛成功");
    }

    /**
     * 测试9：强制结束比赛
     */
    @Test
    @Order(9)
    public void testForceFinishCompetition() {
        // 先开始比赛
        competitionService.startCompetition(testCompetition.getId());

        // 强制结束比赛
        String reason = "测试强制结束功能";
        Competition finished = competitionService.forceFinishCompetition(
                testCompetition.getId(), reason);

        assertNotNull(finished);
        assertEquals("COMPLETED", finished.getStatus());
        assertNotNull(finished.getCompletedAt());
        assertNotNull(finished.getEndedAt());
        assertTrue(finished.getDurationSeconds() >= 0);

        System.out.println("✓ 测试9通过：强制结束比赛成功");
        System.out.println("  比赛状态: " + finished.getStatus());
        System.out.println("  持续时间: " + finished.getDurationSeconds() + " 秒");
    }

    /**
     * 测试10：导出比赛成绩 - Excel格式
     */
    @Test
    @Order(10)
    public void testExportCompetitionResults_Excel() {
        // 准备比赛数据
        setupCompletedCompetitionWithResults();

        ExportFile exportFile = competitionService.exportCompetitionResults(
                testCompetition.getId(), "xlsx");

        assertNotNull(exportFile);
        assertNotNull(exportFile.getFileName());
        assertNotNull(exportFile.getData());
        assertTrue(exportFile.getData().length > 0);
        assertTrue(exportFile.getFileName().endsWith(".xlsx"));
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                exportFile.getContentType());

        System.out.println("✓ 测试10通过：导出比赛成绩（Excel）成功");
        System.out.println("  文件名: " + exportFile.getFileName());
        System.out.println("  文件大小: " + exportFile.getData().length + " 字节");
    }

    /**
     * 测试11：导出比赛成绩 - CSV格式
     */
    @Test
    @Order(11)
    public void testExportCompetitionResults_CSV() {
        setupCompletedCompetitionWithResults();

        ExportFile exportFile = competitionService.exportCompetitionResults(
                testCompetition.getId(), "csv");

        assertNotNull(exportFile);
        assertNotNull(exportFile.getFileName());
        assertNotNull(exportFile.getData());
        assertTrue(exportFile.getData().length > 0);
        assertTrue(exportFile.getFileName().endsWith(".csv"));
        assertEquals("text/csv;charset=UTF-8", exportFile.getContentType());

        String csvContent = new String(exportFile.getData());
        assertTrue(csvContent.contains("Rank"));
        assertTrue(csvContent.contains("Athlete"));

        System.out.println("✓ 测试11通过：导出比赛成绩（CSV）成功");
        System.out.println("  CSV内容预览: " + csvContent.substring(0, Math.min(100, csvContent.length())));
    }

    /**
     * 测试12：生成比赛结果PDF报告
     */
    @Test
    @Order(12)
    public void testGenerateCompetitionResultPDF() {
        setupCompletedCompetitionWithResults();

        byte[] pdfBytes = competitionService.getCompetitionResultsAsPdf(testCompetition.getId());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // PDF文件应该以 %PDF 开头
        String header = new String(Arrays.copyOfRange(pdfBytes, 0, 4));
        assertEquals("%PDF", header);

        System.out.println("✓ 测试12通过：生成比赛结果PDF报告成功");
        System.out.println("  PDF大小: " + pdfBytes.length + " 字节");
    }

    /**
     * 测试13：比赛配置验证 - 非法参数
     */
    @Test
    @Order(13)
    public void testCompetitionConfigValidation() {
        StpUtil.login(adminUser.getId());

        // 测试非法轮数
        Competition invalidRounds = new Competition();
        invalidRounds.setName("非法轮数比赛");
        invalidRounds.setRoundsCount(0); // 非法
        invalidRounds.setShotsPerRound(10);

        assertThrows(RuntimeException.class, () -> {
            competitionService.createCompetition(invalidRounds);
        });

        // 测试非法每轮射击次数
        Competition invalidShots = new Competition();
        invalidShots.setName("非法射击次数比赛");
        invalidShots.setRoundsCount(3);
        invalidShots.setShotsPerRound(-1); // 非法

        assertThrows(RuntimeException.class, () -> {
            competitionService.createCompetition(invalidShots);
        });

        // 测试非法报名时间
        Competition invalidEnrollTime = new Competition();
        invalidEnrollTime.setName("非法报名时间比赛");
        invalidEnrollTime.setRoundsCount(3);
        invalidEnrollTime.setShotsPerRound(10);
        invalidEnrollTime.setEnrollStartAt(LocalDateTime.now().plusDays(10));
        invalidEnrollTime.setEnrollEndAt(LocalDateTime.now()); // 结束时间早于开始时间

        assertThrows(RuntimeException.class, () -> {
            competitionService.createCompetition(invalidEnrollTime);
        });

        System.out.println("✓ 测试13通过：比赛配置验证正确拦截非法参数");
    }

    /**
     * 测试14：比赛状态流转
     */
    @Test
    @Order(14)
    public void testCompetitionStatusTransition() {
        Competition comp = new Competition();
        comp.setName("状态流转测试比赛");
        comp.setRoundsCount(3);
        comp.setShotsPerRound(10);
        comp.setAccessLevel("PUBLIC");
        comp.setEnrollStartAt(LocalDateTime.now());
        comp.setEnrollEndAt(LocalDateTime.now().plusDays(1));
        competitionMapper.insert(comp);

        // CREATED -> RUNNING
        Competition running = competitionService.startCompetition(comp.getId());
        assertEquals("RUNNING", running.getStatus());

        // RUNNING -> PAUSED
        Competition paused = competitionService.pauseCompetition(comp.getId());
        assertEquals("PAUSED", paused.getStatus());

        // PAUSED -> RUNNING
        Competition resumed = competitionService.resumeCompetition(comp.getId());
        assertEquals("RUNNING", resumed.getStatus());

        // RUNNING -> COMPLETED
        Competition completed = competitionService.completeCompetition(comp.getId());
        assertEquals("COMPLETED", completed.getStatus());
        assertNotNull(completed.getCompletedAt());

        System.out.println("✓ 测试14通过：比赛状态流转正确");
    }

    /**
     * 测试15：已开始的比赛不允许修改配置
     */
    @Test
    @Order(15)
    public void testCannotUpdateRunningCompetition() {
        Competition comp = new Competition();
        comp.setName("运行中的比赛");
        comp.setRoundsCount(3);
        comp.setShotsPerRound(10);
        comp.setAccessLevel("PUBLIC");
        comp.setEnrollStartAt(LocalDateTime.now());
        comp.setEnrollEndAt(LocalDateTime.now().plusDays(1));
        competitionMapper.insert(comp);

        // 开始比赛
        competitionService.startCompetition(comp.getId());

        // 尝试修改配置
        Competition update = new Competition();
        update.setId(comp.getId());
        update.setName("尝试修改");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            competitionService.updateCompetition(update);
        });

        assertTrue(exception.getMessage().contains("已开始"));
        System.out.println("✓ 测试15通过：正确阻止修改运行中比赛的配置");
    }

    /**
     * 辅助方法：准备包含结果的已完成比赛
     */
    private void setupCompletedCompetitionWithResults() {
        // 确保比赛已完成
        Competition comp = competitionMapper.findById(testCompetition.getId());
        if (!"COMPLETED".equals(comp.getStatus())) {
            comp.setStatus("COMPLETED");
            comp.setCompletedAt(LocalDateTime.now());
            comp.setStartedAt(LocalDateTime.now().minusHours(2));
            comp.setDurationSeconds(7200);
            competitionMapper.update(comp);
        }

        // 清理旧结果
        competitionResultMapper.deleteByCompetitionId(testCompetition.getId());

        // 创建测试成绩
        CompetitionResult result1 = new CompetitionResult();
        result1.setCompetitionId(testCompetition.getId());
        result1.setAthleteId(athlete1.getId());
        result1.setAthleteName(athlete1.getName());
        result1.setFinalRank(1);
        result1.setFinalScore(BigDecimal.valueOf(95.5));
        result1.setTotalShots(30);
        result1.setCreatedAt(LocalDateTime.now());

        CompetitionResult result2 = new CompetitionResult();
        result2.setCompetitionId(testCompetition.getId());
        result2.setAthleteId(athlete2.getId());
        result2.setAthleteName(athlete2.getName());
        result2.setFinalRank(2);
        result2.setFinalScore(BigDecimal.valueOf(92.3));
        result2.setTotalShots(30);
        result2.setCreatedAt(LocalDateTime.now());

        competitionResultMapper.batchInsert(Arrays.asList(result1, result2));
    }
}

