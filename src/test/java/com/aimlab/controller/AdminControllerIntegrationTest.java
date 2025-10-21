package com.aimlab.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.entity.User;
import com.aimlab.entity.Athlete;
import com.aimlab.mapper.UserMapper;
import com.aimlab.mapper.AthleteMapper;
import com.aimlab.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 管理员控制器集成测试
 * 测试管理员身份切换、权限校验、仪表盘聚合、导出文件等功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AthleteMapper athleteMapper;

    @Autowired
    private AdminService adminService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User adminUser;
    private User normalUser;
    private Long adminUserId;
    private Long normalUserId;

    @BeforeEach
    public void setup() {
        // 创建管理员用户
        adminUser = new User();
        adminUser.setUsername("admin_test");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole("admin");
        adminUser.setStatus(1);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(adminUser);
        adminUserId = adminUser.getId();

        // 创建普通用户
        normalUser = new User();
        normalUser.setUsername("normal_test");
        normalUser.setPassword(passwordEncoder.encode("user123"));
        normalUser.setRole("user");
        normalUser.setStatus(1);
        normalUser.setCreatedAt(LocalDateTime.now());
        normalUser.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(normalUser);
        normalUserId = normalUser.getId();

        // 创建测试运动员数据
        createTestAthletes();
    }

    @AfterEach
    public void cleanup() {
        // 清理登录状态
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    private void createTestAthletes() {
        for (int i = 1; i <= 5; i++) {
            // 为每个运动员创建独立的用户
            User athleteUser = new User();
            athleteUser.setUsername("athlete_user_" + i);
            athleteUser.setPassword(passwordEncoder.encode("athlete" + i));
            athleteUser.setRole("user");
            athleteUser.setStatus(1);
            athleteUser.setCreatedAt(LocalDateTime.now());
            athleteUser.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(athleteUser);
            
            // 创建运动员档案
            Athlete athlete = new Athlete();
            athlete.setUserId(athleteUser.getId());
            athlete.setName("运动员" + i);
            athlete.setGender(i % 2 == 0 ? "FEMALE" : "MALE");
            athlete.setBirthDate(java.time.LocalDate.of(1990 + i, 1, 1));
            athlete.setLevel(i % 3 == 0 ? "国家级" : "省级");
            athlete.setApprovalStatus(i % 2 == 0 ? "APPROVED" : "PENDING");
            athlete.setCreatedAt(LocalDateTime.now());
            athlete.setUpdatedAt(LocalDateTime.now());
            athleteMapper.insert(athlete);
        }
    }

    /**
     * 测试1：管理员身份权限校验 - 成功访问仪表盘
     */
    @Test
    @Order(1)
    public void testAdminAccessDashboard_Success() throws Exception {
        // 模拟管理员登录
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metrics").exists())
                .andExpect(jsonPath("$.metrics.overview").exists())
                .andExpect(jsonPath("$.metrics.overview.totalUsers").exists())
                .andExpect(jsonPath("$.metrics.todos").exists());

        System.out.println("✓ 测试1通过：管理员成功访问仪表盘");
    }

    /**
     * 测试2：普通用户权限校验 - 禁止访问仪表盘
     */
    @Test
    @Order(2)
    public void testNormalUserAccessDashboard_Forbidden() throws Exception {
        // 模拟普通用户登录
        StpUtil.login(normalUserId);
        StpUtil.getSession().set("role", "user");

        // 普通用户访问管理员仪表盘应该被拒绝
        try {
            mockMvc.perform(get("/api/admin/dashboard")
                            .header("satoken", StpUtil.getTokenValue()))
                    .andExpect(status().is4xxClientError());
            
            System.out.println("✓ 测试2通过：普通用户被正确拒绝访问管理员仪表盘");
        } catch (Exception e) {
            // 如果抛出权限异常，也视为测试通过
            assertTrue(e.getMessage().contains("权限") || e.getMessage().contains("permission"));
            System.out.println("✓ 测试2通过：普通用户被权限校验拦截");
        }
    }

    /**
     * 测试3：仪表盘数据聚合测试
     */
    @Test
    @Order(3)
    public void testDashboardMetricsAggregation() {
        Map<String, Object> metrics = adminService.getDashboardMetrics();

        assertNotNull(metrics);
        assertTrue(metrics.containsKey("overview"));
        assertTrue(metrics.containsKey("todos"));

        @SuppressWarnings("unchecked")
        Map<String, Object> overview = (Map<String, Object>) metrics.get("overview");
        assertNotNull(overview.get("totalUsers"));
        assertNotNull(overview.get("activeTrainings"));
        assertNotNull(overview.get("activeCompetitions"));
        assertNotNull(overview.get("recentReports"));

        @SuppressWarnings("unchecked")
        Map<String, Object> todos = (Map<String, Object>) metrics.get("todos");
        assertNotNull(todos.get("pendingAthletes"));
        assertNotNull(todos.get("upcomingCompetitions"));

        System.out.println("✓ 测试3通过：仪表盘数据聚合正确");
        System.out.println("  - 总用户数: " + overview.get("totalUsers"));
        System.out.println("  - 待审核运动员: " + 
            ((Map<?, ?>) todos.get("pendingAthletes")).get("total"));
    }

    /**
     * 测试4：获取用户列表（分页）
     */
    @Test
    @Order(4)
    public void testGetUsersList() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        MvcResult result = mockMvc.perform(get("/api/admin/users")
                        .param("page", "1")
                        .param("size", "10")
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.items").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println("✓ 测试4通过：获取用户列表成功");
        System.out.println("  响应内容: " + content);
    }

    /**
     * 测试5：创建新用户
     */
    @Test
    @Order(5)
    public void testCreateUser() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", "newuser_test");
        newUser.put("password", "password123");
        newUser.put("role", "user");
        newUser.put("status", 1);

        MvcResult result = mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser))
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println("✓ 测试5通过：创建新用户成功");
        System.out.println("  响应内容: " + content);
    }

    /**
     * 测试6：更新用户信息（角色和状态）
     */
    @Test
    @Order(6)
    public void testUpdateUser() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("role", "coach");
        updateData.put("status", 0);

        mockMvc.perform(put("/api/admin/users/" + normalUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData))
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        System.out.println("✓ 测试6通过：更新用户信息成功");
    }

    /**
     * 测试7：重置用户密码
     */
    @Test
    @Order(7)
    public void testResetPassword() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        Map<String, Object> resetData = new HashMap<>();
        resetData.put("password", "newpassword123");

        mockMvc.perform(post("/api/admin/users/" + normalUserId + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetData))
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✓ 测试7通过：重置用户密码成功");
    }

    /**
     * 测试8：获取运动员列表
     */
    @Test
    @Order(8)
    public void testGetAthletesList() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        MvcResult result = mockMvc.perform(get("/api/admin/athletes")
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.athletes").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println("✓ 测试8通过：获取运动员列表成功");
        System.out.println("  响应内容: " + content);
    }

    /**
     * 测试9：导出运动员档案（CSV格式）
     */
    @Test
    @Order(9)
    public void testExportAthletes_CSV() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        MvcResult result = mockMvc.perform(get("/api/admin/athletes/export")
                        .param("format", "csv")
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 0);
        System.out.println("✓ 测试9通过：导出运动员档案（CSV）成功，文件大小: " + content.length + " 字节");
    }

    /**
     * 测试10：导出运动员档案（Excel格式）
     */
    @Test
    @Order(10)
    public void testExportAthletes_Excel() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        MvcResult result = mockMvc.perform(get("/api/admin/athletes/export")
                        .param("format", "xlsx")
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 0);
        System.out.println("✓ 测试10通过：导出运动员档案（Excel）成功，文件大小: " + content.length + " 字节");
    }

    /**
     * 测试11：导入运动员档案（CSV格式）
     */
    @Test
    @Order(11)
    public void testImportAthletes_CSV() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        // 创建测试CSV文件内容
        String csvContent = "name,gender,birthDate,level\n" +
                "张三,男,1995-01-01,国家级\n" +
                "李四,女,1996-05-15,省级\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "athletes.csv",
                "text/csv",
                csvContent.getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/admin/athletes/import")
                        .file(file)
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.imported").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println("✓ 测试11通过：导入运动员档案（CSV）成功");
        System.out.println("  响应内容: " + content);
    }

    /**
     * 测试12：强制踢出用户
     */
    @Test
    @Order(12)
    public void testKickUser() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        // 先让普通用户登录
        StpUtil.login(normalUserId);
        // 验证普通用户已登录
        assertEquals(normalUserId, StpUtil.getLoginIdAsLong());

        // 管理员踢出普通用户
        mockMvc.perform(post("/api/admin/users/" + normalUserId + "/kick")
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✓ 测试12通过：强制踢出用户成功");
    }

    /**
     * 测试13：按条件筛选用户
     */
    @Test
    @Order(13)
    public void testFilterUsers() throws Exception {
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        // 按角色筛选
        mockMvc.perform(get("/api/admin/users")
                        .param("role", "admin")
                        .param("page", "1")
                        .param("size", "10")
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.items").isArray());

        System.out.println("✓ 测试13通过：按条件筛选用户成功");
    }

    /**
     * 测试14：删除用户
     */
    @Test
    @Order(14)
    public void testDeleteUser() throws Exception {
        // 创建一个临时用户用于删除
        User tempUser = new User();
        tempUser.setUsername("temp_delete_user");
        tempUser.setPassword(passwordEncoder.encode("temp123"));
        tempUser.setRole("user");
        tempUser.setStatus(1);
        tempUser.setCreatedAt(LocalDateTime.now());
        tempUser.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(tempUser);
        Long tempUserId = tempUser.getId();

        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");

        mockMvc.perform(delete("/api/admin/users/" + tempUserId)
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✓ 测试14通过：删除用户成功");
    }

    /**
     * 测试15：测试身份切换场景
     */
    @Test
    @Order(15)
    public void testRoleSwitching() throws Exception {
        // 场景1：管理员登录并访问
        StpUtil.login(adminUserId);
        StpUtil.getSession().set("role", "admin");
        
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("satoken", StpUtil.getTokenValue()))
                .andExpect(status().isOk());
        
        StpUtil.logout();

        // 场景2：普通用户登录后尝试访问管理员接口（应该失败）
        StpUtil.login(normalUserId);
        StpUtil.getSession().set("role", "user");

        try {
            mockMvc.perform(get("/api/admin/dashboard")
                            .header("satoken", StpUtil.getTokenValue()))
                    .andExpect(status().is4xxClientError());
        } catch (Exception e) {
            // 权限异常也算测试通过
            assertTrue(e.getMessage().contains("权限") || e.getMessage().contains("permission"));
        }

        System.out.println("✓ 测试15通过：身份切换场景测试成功");
    }
}

