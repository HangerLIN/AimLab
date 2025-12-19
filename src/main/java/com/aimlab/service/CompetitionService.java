package com.aimlab.service;

import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.dto.ExportFile;
import com.aimlab.dto.RankingItemDTO;
import com.aimlab.entity.Competition;
import com.aimlab.entity.CompetitionAthlete;
import com.aimlab.entity.CompetitionResult;
import com.aimlab.entity.CompetitionStatus;
import com.aimlab.entity.ShootingRecord;
import com.aimlab.entity.Athlete;
import com.aimlab.mapper.CompetitionAthleteMapper;
import com.aimlab.mapper.AthleteMapper;
import com.aimlab.mapper.CompetitionMapper;
import com.aimlab.mapper.CompetitionResultMapper;
import com.aimlab.mapper.ShootingRecordMapper;
import com.aimlab.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 比赛服务类
 */
@Service
public class CompetitionService {

    @Autowired
    private CompetitionMapper competitionMapper;
    
    @Autowired
    private CompetitionAthleteMapper competitionAthleteMapper;
    
    @Autowired
    private AthleteMapper athleteMapper;
    
    @Autowired
    private ShootingRecordMapper shootingRecordMapper;
    
    @Autowired
    private CompetitionResultMapper competitionResultMapper;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private PdfGenerationService pdfGenerationService;
    
    /**
     * 比赛状态缓存，使用内存存储当前正在进行的比赛状态
     * Key: 比赛ID, Value: 比赛状态对象
     */
    private final Map<Long, CompetitionStatus> competitionStatusMap = new ConcurrentHashMap<>();

    private static final Set<String> ALLOWED_ACCESS_LEVELS = Set.of("PUBLIC", "ADMIN_ONLY");
    private static final String DEFAULT_ACCESS_LEVEL = "PUBLIC";
    private static final String DEFAULT_FORMAT_TYPE = "STANDARD";
    
    /**
     * 创建新比赛
     * 
     * @param competition 比赛对象
     * @return 创建的比赛对象
     */
    @Transactional
    public Competition createCompetition(Competition competition) {
        // 设置创建时间和初始状态
        competition.setCreatedAt(LocalDateTime.now());
        competition.setStatus("CREATED");
        competition.setFormatType(normalizeFormatType(competition.getFormatType()));
        competition.setAccessLevel(normalizeAccessLevel(competition.getAccessLevel()));
        validateCompetitionConfig(competition);
        if (competition.getCreatedBy() == null && StpUtil.isLogin()) {
            competition.setCreatedBy(StpUtil.getLoginIdAsLong());
        }
        
        // 保存比赛
        competitionMapper.insert(competition);
        
        return competition;
    }
    
    /**
     * 获取比赛详情
     * 
     * @param competitionId 比赛ID
     * @return 比赛对象
     */
    public Competition getCompetitionById(Integer competitionId) {
        return competitionMapper.findById(competitionId);
    }
    
    /**
     * 获取比赛列表
     * 
     * @return 比赛列表
     */
    public List<Competition> getAllCompetitions() {
        return competitionMapper.findAll();
    }
    
    /**
     * 更新比赛信息
     * 
     * @param competition 比赛对象
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition updateCompetition(Competition competition) {
        // 检查比赛是否存在
        Competition existingCompetition = competitionMapper.findById(competition.getId());
        if (existingCompetition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 不允许修改已开始的比赛
        if ("RUNNING".equals(existingCompetition.getStatus()) || 
            "PAUSED".equals(existingCompetition.getStatus())) {
            throw new RuntimeException("比赛已开始，无法修改基本信息");
        }
        
        mergeCompetitionConfig(existingCompetition, competition);
        validateCompetitionConfig(existingCompetition);
        competitionMapper.update(existingCompetition);
        
        return existingCompetition;
    }

    private void mergeCompetitionConfig(Competition existing, Competition incoming) {
        if (incoming.getName() != null) {
            existing.setName(incoming.getName());
        }
        if (incoming.getDescription() != null) {
            existing.setDescription(incoming.getDescription());
        }
        if (incoming.getFormatType() != null) {
            existing.setFormatType(normalizeFormatType(incoming.getFormatType()));
        }
        if (incoming.getRoundsCount() != null) {
            existing.setRoundsCount(incoming.getRoundsCount());
        }
        if (incoming.getShotsPerRound() != null) {
            existing.setShotsPerRound(incoming.getShotsPerRound());
        }
        if (incoming.getTimeLimitPerShot() != null) {
            existing.setTimeLimitPerShot(incoming.getTimeLimitPerShot());
        }
        if (incoming.getEnrollStartAt() != null) {
            existing.setEnrollStartAt(incoming.getEnrollStartAt());
        }
        if (incoming.getEnrollEndAt() != null) {
            existing.setEnrollEndAt(incoming.getEnrollEndAt());
        }
        if (incoming.getAccessLevel() != null) {
            existing.setAccessLevel(normalizeAccessLevel(incoming.getAccessLevel()));
        }
    }

    private void validateCompetitionConfig(Competition competition) {
        if (competition.getRoundsCount() != null && competition.getRoundsCount() <= 0) {
            throw new RuntimeException("总轮数必须大于0");
        }
        if (competition.getShotsPerRound() != null && competition.getShotsPerRound() <= 0) {
            throw new RuntimeException("每轮射击次数必须大于0");
        }
        if (competition.getTimeLimitPerShot() != null && competition.getTimeLimitPerShot() < 0) {
            throw new RuntimeException("时间限制不能为负");
        }
        LocalDateTime enrollStart = competition.getEnrollStartAt();
        LocalDateTime enrollEnd = competition.getEnrollEndAt();
        if (enrollStart != null && enrollEnd != null && enrollStart.isAfter(enrollEnd)) {
            throw new RuntimeException("报名开始时间不能晚于结束时间");
        }
        if (competition.getAccessLevel() == null) {
            competition.setAccessLevel(DEFAULT_ACCESS_LEVEL);
        }
        if (!ALLOWED_ACCESS_LEVELS.contains(competition.getAccessLevel())) {
            throw new RuntimeException("无效的报名权限配置");
        }
        if (competition.getFormatType() == null) {
            competition.setFormatType(DEFAULT_FORMAT_TYPE);
        }
    }

    private String normalizeAccessLevel(String accessLevel) {
        if (accessLevel == null || accessLevel.trim().isEmpty()) {
            return DEFAULT_ACCESS_LEVEL;
        }
        String normalized = accessLevel.trim().toUpperCase();
        return ALLOWED_ACCESS_LEVELS.contains(normalized) ? normalized : DEFAULT_ACCESS_LEVEL;
    }

    private String normalizeFormatType(String formatType) {
        if (formatType == null || formatType.trim().isEmpty()) {
            return DEFAULT_FORMAT_TYPE;
        }
        return formatType.trim().toUpperCase();
    }

    private boolean isEnrollmentOpen(Competition competition) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = competition.getEnrollStartAt();
        LocalDateTime end = competition.getEnrollEndAt();
        if (start != null && now.isBefore(start)) {
            return false;
        }
        if (end != null && now.isAfter(end)) {
            return false;
        }
        return true;
    }

    private boolean isAdminOnlyAccess(Competition competition) {
        return "ADMIN_ONLY".equalsIgnoreCase(competition.getAccessLevel());
    }

    private boolean isCurrentUserAdmin() {
        try {
            return StpUtil.isLogin() && StpUtil.hasPermission("admin:competitions.manage");
        } catch (Exception ex) {
            return false;
        }
    }
    
    /**
     * 删除比赛
     * 
     * @param competitionId 比赛ID
     */
    @Transactional
    public void deleteCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 不允许删除已开始的比赛
        if ("RUNNING".equals(competition.getStatus()) || 
            "PAUSED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已开始，无法删除");
        }
        
        // 删除比赛参赛运动员
        competitionAthleteMapper.deleteByCompetitionId(competitionId);
        
        // 删除比赛
        competitionMapper.delete(competitionId.longValue());
    }
    
    /**
     * 运动员报名参赛
     * 
     * @param competitionAthlete 比赛运动员对象
     * @return 报名结果
     */
    @Transactional
    public CompetitionAthlete registerAthlete(CompetitionAthlete competitionAthlete) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionAthlete.getCompetitionId());
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛是否已开始
        if ("RUNNING".equals(competition.getStatus()) || 
            "PAUSED".equals(competition.getStatus()) || 
            "COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已开始或已结束，无法报名");
        }

        if (!isEnrollmentOpen(competition)) {
            throw new RuntimeException("当前不在报名时间窗口内");
        }

        if (isAdminOnlyAccess(competition) && !isCurrentUserAdmin()) {
            throw new RuntimeException("本场比赛仅限管理员登记参赛");
        }
        
        // 检查运动员是否存在并获取审批状态
        com.aimlab.entity.Athlete athlete = athleteMapper.findById(competitionAthlete.getAthleteId());
        if (athlete == null) {
            throw new RuntimeException("运动员不存在");
        }
        
        // 检查运动员档案是否已被批准
        if (athlete.getApprovalStatus() == null || !"APPROVED".equals(athlete.getApprovalStatus())) {
            throw new RuntimeException("运动员档案未被批准，无法报名比赛");
        }
        
        // 检查运动员是否已报名
        CompetitionAthlete existingRegistration = competitionAthleteMapper.findByCompetitionIdAndAthleteId(
                competitionAthlete.getCompetitionId(), competitionAthlete.getAthleteId());
        if (existingRegistration != null) {
            throw new RuntimeException("运动员已报名该比赛");
        }
        
        // 设置报名时间
        competitionAthlete.setRegisteredAt(LocalDateTime.now());
        
        // 保存报名信息
        competitionAthleteMapper.insert(competitionAthlete);
        
        return competitionAthlete;
    }
    
    /**
     * 取消运动员报名
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     */
    @Transactional
    public void unregisterAthlete(Integer competitionId, Long athleteId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛是否已开始
        if ("RUNNING".equals(competition.getStatus()) || 
            "PAUSED".equals(competition.getStatus()) || 
            "COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已开始或已结束，无法取消报名");
        }
        
        // 检查运动员是否已报名
        CompetitionAthlete existingRegistration = competitionAthleteMapper.findByCompetitionIdAndAthleteId(
                competitionId, athleteId);
        if (existingRegistration == null) {
            throw new RuntimeException("运动员未报名该比赛");
        }
        
        // 删除报名信息
        competitionAthleteMapper.delete(existingRegistration.getId());
    }
    
    /**
     * 获取比赛参赛运动员列表
     * 
     * @param competitionId 比赛ID
     * @return 参赛运动员列表
     */
    public List<CompetitionAthlete> getCompetitionAthletes(Integer competitionId) {
        return competitionAthleteMapper.findByCompetitionId(competitionId);
    }
    
    /**
     * 开始比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition startCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if ("RUNNING".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已经开始");
        }
        if ("COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已经结束");
        }
        
        // 检查是否有参赛运动员（暂时注释掉以便测试）
        // List<CompetitionAthlete> athletes = competitionAthleteMapper.findByCompetitionId(competitionId);
        // if (athletes.isEmpty()) {
        //     throw new RuntimeException("没有运动员报名，无法开始比赛");
        // }
        
        // 更新比赛状态
        competition.setStatus("RUNNING");
        competition.setStartedAt(LocalDateTime.now());
        competitionMapper.update(competition);
        
        // 创建并存储比赛状态
        CompetitionStatus status = new CompetitionStatus();
        status.setCompetitionId(competitionId.longValue());
        status.setStartTime(competition.getStartedAt());
        status.setStatus("RUNNING");
        competitionStatusMap.put(competitionId.longValue(), status);
        
        // 通过WebSocket广播比赛开始消息
        try {
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "RUNNING", 
                "比赛已开始");
            System.out.println("比赛已开始: " + competitionId + " 状态: RUNNING，WebSocket广播成功");
        } catch (Exception e) {
            // WebSocket发送失败时记录日志但不影响比赛开始
            System.out.println("WebSocket广播失败: " + e.getMessage());
            System.out.println("比赛已开始: " + competitionId + " 状态: RUNNING（WebSocket广播失败但比赛正常开始）");
        }
        
        return competition;
    }
    
    /**
     * 暂停比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition pauseCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if (!"RUNNING".equals(competition.getStatus())) {
            throw new RuntimeException("比赛未开始，无法暂停");
        }
        
        // 更新比赛状态
        competition.setStatus("PAUSED");
        competitionMapper.update(competition);
        
        // 更新内存中的比赛状态
        CompetitionStatus status = competitionStatusMap.get(competitionId.longValue());
        if (status != null) {
            status.setStatus("PAUSED");
            status.setPauseTime(LocalDateTime.now());
        }
        
        // 通过WebSocket广播比赛暂停消息
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "PAUSED", 
                "比赛已暂停");
        
        return competition;
    }
    
    /**
     * 恢复比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition resumeCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if (!"PAUSED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛未暂停，无法恢复");
        }
        
        // 更新比赛状态
        competition.setStatus("RUNNING");
        competitionMapper.update(competition);
        
        // 更新内存中的比赛状态
        CompetitionStatus status = competitionStatusMap.get(competitionId.longValue());
        if (status != null) {
            status.setStatus("RUNNING");
            status.setResumeTime(LocalDateTime.now());
        }
        
        // 通过WebSocket广播比赛恢复消息
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "RUNNING", 
                "比赛已恢复");
        
        return competition;
    }
    
    /**
     * 完成比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition completeCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if ("COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已经结束");
        }
        if (!"RUNNING".equals(competition.getStatus()) && !"PAUSED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛未开始，无法结束");
        }
        
        // 更新比赛状态
        competition.setStatus("COMPLETED");
        competition.setCompletedAt(LocalDateTime.now());
        
        // 计算比赛持续时间（秒）
        CompetitionStatus status = competitionStatusMap.get(competitionId.longValue());
        if (status != null) {
            long durationSeconds = status.calculateTotalDuration();
            competition.setDurationSeconds((int) durationSeconds);
        } else if (competition.getStartedAt() != null && competition.getCompletedAt() != null) {
            long durationSeconds = ChronoUnit.SECONDS.between(
                    competition.getStartedAt(), 
                    competition.getCompletedAt());
            competition.setDurationSeconds((int) durationSeconds);
        }
        
        competitionMapper.update(competition);
        
        // 计算并保存最终成绩
        calculateAndSaveFinalResults(competitionId);
        
        // 通过WebSocket广播比赛结束消息
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "COMPLETED", 
                "比赛已结束");
        
        return competition;
    }

    /**
     * 管理员强制结束比赛
     *
     * @param competitionId 比赛ID
     * @param reason        强制结束原因
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition forceFinishCompetition(Integer competitionId, String reason) {
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }

        if ("COMPLETED".equals(competition.getStatus())) {
            return competition;
        }

        LocalDateTime now = LocalDateTime.now();
        competition.setStatus("COMPLETED");
        competition.setEndedAt(now);
        competition.setCompletedAt(now);

        if (competition.getStartedAt() != null) {
            long durationSeconds = ChronoUnit.SECONDS.between(competition.getStartedAt(), now);
            competition.setDurationSeconds((int) Math.max(durationSeconds, 0));
        }

        competitionMapper.update(competition);

        // 删除内存中的状态缓存并生成最终成绩
        competitionStatusMap.remove(competitionId.longValue());
        calculateAndSaveFinalResults(competitionId);

        String message = "比赛已被管理员强制结束";
        if (reason != null && !reason.trim().isEmpty()) {
            message = message + "，原因：" + reason.trim();
        }

        try {
            webSocketService.sendCompetitionStatusUpdate(
                    String.valueOf(competitionId),
                    "COMPLETED",
                    message);
        } catch (Exception e) {
            System.out.println("强制结束比赛的WebSocket广播失败: " + e.getMessage());
        }

        return competition;
    }
    
    /**
     * 计算并保存最终比赛结果
     * 
     * @param competitionId 比赛ID
     */
    @Transactional
    private void calculateAndSaveFinalResults(Integer competitionId) {
        // 检查是否已经有结果记录
        int count = competitionResultMapper.countByCompetitionId(competitionId);
        if (count > 0) {
            // 如果已经有结果记录，先删除
            competitionResultMapper.deleteByCompetitionId(competitionId);
        }
        
        // 获取最终排名数据
        List<RankingItemDTO> rankings = getLiveRanking(competitionId);
        if (rankings == null || rankings.isEmpty()) {
            return; // 没有排名数据，直接返回
        }
        
        // 创建结果记录列表
        List<CompetitionResult> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 遍历排名数据，创建结果记录
        for (int i = 0; i < rankings.size(); i++) {
            RankingItemDTO ranking = rankings.get(i);
            
            CompetitionResult result = new CompetitionResult();
            result.setCompetitionId(competitionId);
            result.setAthleteId(ranking.getAthleteId());
            result.setAthleteName(ranking.getAthleteName());
            result.setFinalRank(i + 1); // 排名从1开始
            result.setFinalScore(ranking.getTotalScore());
            result.setTotalShots(ranking.getTotalShots());
            result.setCreatedAt(now);
            
            results.add(result);
        }
        
        // 批量保存结果记录
        if (!results.isEmpty()) {
            competitionResultMapper.batchInsert(results);
        }
    }
    
    /**
     * 获取比赛最终结果
     * 
     * @param competitionId 比赛ID
     * @return 比赛结果列表
     */
    public List<CompetitionResult> getCompetitionResults(Integer competitionId) {
        return competitionResultMapper.findByCompetitionId(competitionId);
    }

    /**
     * 生成比赛结果PDF
     *
     * @param competitionId 比赛ID
     * @return PDF字节流
     */
    public byte[] getCompetitionResultsAsPdf(Integer competitionId) {
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        if (!"COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛尚未结束，无法生成结果报告");
        }

        List<CompetitionResult> results = competitionResultMapper.findByCompetitionId(competitionId);
        return pdfGenerationService.generateCompetitionResultsPdf(competition, results);
    }

    public ExportFile exportCompetitionResults(Integer competitionId, String format) {
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        if (!"COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛尚未结束，无法导出成绩");
        }
        List<CompetitionResult> results = competitionResultMapper.findByCompetitionId(competitionId);
        String normalizedFormat = format != null ? format.trim().toLowerCase() : "xlsx";
        if ("csv".equals(normalizedFormat)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Rank,Athlete ID,Athlete Name,Final Score,Total Shots\n");
            for (CompetitionResult result : results) {
                sb.append(result.getFinalRank()).append(',')
                        .append(result.getAthleteId()).append(',')
                        .append(escapeCsv(result.getAthleteName())).append(',')
                        .append(result.getFinalScore()).append(',')
                        .append(result.getTotalShots()).append('\n');
            }
            String fileName = "competition-" + competitionId + "-results.csv";
            return new ExportFile(fileName, "text/csv;charset=UTF-8", sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Results");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("名次");
            header.createCell(1).setCellValue("运动员ID");
            header.createCell(2).setCellValue("运动员姓名");
            header.createCell(3).setCellValue("总成绩");
            header.createCell(4).setCellValue("射击次数");

            int rowIndex = 1;
            for (CompetitionResult result : results) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(result.getFinalRank());
                row.createCell(1).setCellValue(result.getAthleteId());
                row.createCell(2).setCellValue(result.getAthleteName());
                row.createCell(3).setCellValue(result.getFinalScore() == null ? 0.0 : result.getFinalScore().doubleValue());
                row.createCell(4).setCellValue(result.getTotalShots());
            }

            workbook.write(bos);
            String fileName = "competition-" + competitionId + "-results.xlsx";
            return new ExportFile(fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("导出比赛成绩失败: " + e.getMessage(), e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }
    
    /**
     * 获取运动员的比赛结果
     * 
     * @param athleteId 运动员ID
     * @return 比赛结果列表
     */
    public List<CompetitionResult> getAthleteResults(Long athleteId) {
        return competitionResultMapper.findByAthleteId(athleteId);
    }
    
    /**
     * 每位运动员在单场比赛中的最大射击次数
     */
    private static final int MAX_SHOTS_PER_ATHLETE = 10;

    /**
     * 添加比赛射击记录
     * 
     * @param record 射击记录对象
     * @return 添加的记录对象
     */
    @Transactional
    public ShootingRecord addCompetitionRecord(ShootingRecord record) {
        if (record == null || record.getCompetitionId() == null) {
            throw new RuntimeException("缺少比赛信息");
        }
        
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(record.getCompetitionId());
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛是否可以射击（已创建或进行中）
        if (!"RUNNING".equals(competition.getStatus()) && !"CREATED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已结束或已取消，无法记录成绩");
        }
        
        // 如果未携带运动员ID，尝试通过 userId 或当前登录用户补全
        if (record.getAthleteId() == null) {
            Athlete currentAthlete = null;
            
            if (record.getUserId() != null) {
                currentAthlete = athleteMapper.findByUserId(record.getUserId());
            }
            
            if (currentAthlete == null && StpUtil.isLogin()) {
                Long currentUserId = StpUtil.getLoginIdAsLong();
                currentAthlete = athleteMapper.findByUserId(currentUserId);
            }
            
            if (currentAthlete == null) {
                throw new RuntimeException("无法获取运动员信息，请确认已登录或提供有效的 userId/athleteId");
            }
            
            record.setAthleteId(currentAthlete.getId());
            if (record.getUserId() == null) {
                record.setUserId(currentAthlete.getUserId());
            }
        }
        
        // 校验运动员报名信息
        CompetitionAthlete competitionAthlete = competitionAthleteMapper.findByCompetitionIdAndAthleteId(
                record.getCompetitionId(), record.getAthleteId());
        if (competitionAthlete == null) {
            throw new RuntimeException("您未报名参加该比赛");
        }
        
        // 检查射击次数限制（每人最多10次）
        int currentShotCount = shootingRecordMapper.countByCompetitionIdAndAthleteId(
                record.getCompetitionId(), record.getAthleteId());
        if (currentShotCount >= MAX_SHOTS_PER_ATHLETE) {
            throw new RuntimeException("您已达到本场比赛的射击次数上限（" + MAX_SHOTS_PER_ATHLETE + "次），无法继续射击");
        }
        
        // 获取运动员信息补充 userId
        Athlete athlete = athleteMapper.findById(record.getAthleteId());
        if (athlete != null && athlete.getUserId() != null) {
            record.setUserId(athlete.getUserId());
        }
        
        // 统一设置记录类型和时间
        record.setRecordType("COMPETITION");
        if (record.getShotAt() == null) {
            record.setShotAt(LocalDateTime.now());
        }
        
        shootingRecordMapper.insert(record);
        
        // 通过WebSocket广播射击记录给所有客户端
        webSocketService.sendShootingRecord(
            String.valueOf(record.getCompetitionId()), 
            record
        );
        
        // 更新排名并广播
        updateAndBroadcastRankings(record.getCompetitionId().longValue());
        
        return record;
    }
    
    /**
     * 获取比赛的射击记录
     * 
     * @param competitionId 比赛ID
     * @return 射击记录列表
     */
    public List<ShootingRecord> getCompetitionRecords(Integer competitionId) {
        return shootingRecordMapper.findByCompetitionId(competitionId);
    }
    
    /**
     * 获取比赛中运动员的射击记录
     * 
     * @param competitionId 比赛ID
     * @param athleteId 运动员ID
     * @return 射击记录列表
     */
    public List<ShootingRecord> getAthleteCompetitionRecords(Integer competitionId, Long athleteId) {
        return shootingRecordMapper.findByCompetitionIdAndAthleteId(competitionId, athleteId);
    }
    
    /**
     * 获取比赛实时排名
     * 
     * @param competitionId 比赛ID
     * @return 排名列表
     */
    public List<RankingItemDTO> getLiveRanking(Integer competitionId) {
        // 实现实时排名计算逻辑
        // 根据比赛ID获取所有射击记录，计算每个运动员的总分和排名
        return calculateRanking(competitionId);
    }
    
    /**
     * 更新并广播比赛排名
     * 
     * @param competitionId 比赛ID
     */
    private void updateAndBroadcastRankings(Long competitionId) {
        // 计算最新排名
        List<RankingItemDTO> rankings = calculateRanking(competitionId.intValue());
        
        // 通过WebSocket广播最新排名
        webSocketService.sendRankingUpdate(String.valueOf(competitionId), rankings);
    }
    
    private List<RankingItemDTO> calculateRanking(Integer competitionId) {
        // 获取比赛的所有参赛运动员
        List<CompetitionAthlete> athletes = competitionAthleteMapper.findByCompetitionId(competitionId);
        
        // 获取比赛的所有射击记录
        List<ShootingRecord> records = shootingRecordMapper.findByCompetitionId(competitionId);
        
        // 计算每个运动员的总分
        Map<Long, RankingItemDTO> athleteScores = new HashMap<>();
        
        for (CompetitionAthlete athlete : athletes) {
            RankingItemDTO rankingItem = new RankingItemDTO();
            rankingItem.setAthleteId(athlete.getAthleteId());
            rankingItem.setAthleteName(athlete.getAthleteName());
            rankingItem.setTotalScore(BigDecimal.ZERO);
            rankingItem.setTotalShots(0);
            athleteScores.put(athlete.getAthleteId(), rankingItem);
        }
        
        // 计算每个运动员的总分和排名
        for (ShootingRecord record : records) {
            RankingItemDTO rankingItem = athleteScores.get(record.getAthleteId());
            if (rankingItem != null) {
                rankingItem.setTotalScore(rankingItem.getTotalScore().add(record.getScore()));
                rankingItem.setTotalShots(rankingItem.getTotalShots() + 1);
                rankingItem.setLastShotTime(record.getShotAt());
            }
        }
        
        // 将结果转换为RankingItemDTO列表
        List<RankingItemDTO> rankings = new ArrayList<>(athleteScores.values());
        
        // 按总分降序排序
        rankings.sort((r1, r2) -> r2.getTotalScore().compareTo(r1.getTotalScore()));
        
        // 添加排名信息
        for (int i = 0; i < rankings.size(); i++) {
            // 排名从1开始
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings;
    }
    
    /**
     * 批量报名运动员
     * 
     * @param competitionId 比赛ID
     * @param athleteIds 运动员ID列表
     * @return 成功报名的运动员数量
     */
    @Transactional
    public int enrollAthletes(Integer competitionId, List<Long> athleteIds) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛是否已开始
        if ("RUNNING".equals(competition.getStatus()) || 
            "PAUSED".equals(competition.getStatus()) || 
            "COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已开始或已结束，无法报名");
        }
        boolean currentUserIsAdmin = isCurrentUserAdmin();
        if (!currentUserIsAdmin && !isEnrollmentOpen(competition)) {
            throw new RuntimeException("当前不在报名时间窗口内");
        }
        if (!currentUserIsAdmin && isAdminOnlyAccess(competition)) {
            throw new RuntimeException("本场比赛仅限管理员登记参赛");
        }
        
        int enrolledCount = 0;
        for (Long athleteId : athleteIds) {
            // 检查运动员是否存在并获取审批状态
            com.aimlab.entity.Athlete athlete = athleteMapper.findById(athleteId);
            if (athlete == null) {
                throw new RuntimeException("运动员 " + athleteId + " 不存在");
            }
            
            // 检查运动员档案是否已被批准
            if (athlete.getApprovalStatus() == null || !"APPROVED".equals(athlete.getApprovalStatus())) {
                throw new RuntimeException("运动员 " + athlete.getName() + " 档案未被批准，无法报名比赛");
            }
            
            // 检查运动员是否已报名
            CompetitionAthlete existingRegistration = competitionAthleteMapper.findByCompetitionIdAndAthleteId(
                    competitionId, athleteId);
            if (existingRegistration != null) {
                continue; // 已报名，跳过
            }
            
            // 创建新的报名记录
            CompetitionAthlete competitionAthlete = new CompetitionAthlete();
            competitionAthlete.setCompetitionId(competitionId);
            competitionAthlete.setAthleteId(athleteId);
            competitionAthlete.setStatus("ENROLLED");
            // 数据库字段有默认值，让数据库自动处理
            LocalDateTime now = LocalDateTime.now();
            competitionAthlete.setCreatedAt(now);
            competitionAthlete.setUpdatedAt(now);
            competitionAthlete.setRegisteredAt(now);
            
            // 保存报名信息
            competitionAthleteMapper.insert(competitionAthlete);
            enrolledCount++;
        }
        
        return enrolledCount;
    }
    
    /**
     * 取消比赛
     * 
     * @param competitionId 比赛ID
     * @return 更新后的比赛对象
     */
    @Transactional
    public Competition cancelCompetition(Integer competitionId) {
        // 检查比赛是否存在
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            throw new RuntimeException("比赛不存在");
        }
        
        // 检查比赛状态
        if ("COMPLETED".equals(competition.getStatus())) {
            throw new RuntimeException("比赛已经结束，无法取消");
        }
        
        // 更新比赛状态
        competition.setStatus("CANCELED");
        competitionMapper.update(competition);
        
        // 如果比赛正在进行中，从内存中移除比赛状态
        competitionStatusMap.remove(competitionId.longValue());
        
        // 通过WebSocket广播比赛取消消息
        webSocketService.sendCompetitionStatusUpdate(
                String.valueOf(competitionId), 
                "CANCELED", 
                "比赛已取消");
        
        return competition;
    }
    
    /**
     * 获取比赛实时状态
     * 
     * @param competitionId 比赛ID
     * @return 比赛实时状态
     */
    public CompetitionStatus getCompetitionStatus(Integer competitionId) {
        // 先从内存中获取运行时状态
        CompetitionStatus status = competitionStatusMap.get(competitionId.longValue());
        
        if (status != null) {
            return status;
        }
        
        // 如果内存中没有，查询数据库中的比赛信息
        Competition competition = competitionMapper.findById(competitionId);
        if (competition == null) {
            return null; // 比赛不存在
        }
        
        // 根据数据库中的状态创建状态对象
        CompetitionStatus dbStatus = new CompetitionStatus();
        dbStatus.setCompetitionId(competitionId.longValue());
        dbStatus.setStatus(competition.getStatus());
        dbStatus.setStartTime(competition.getStartedAt());
        
        return dbStatus;
    }
    
    /**
     * 根据状态获取比赛列表
     * 
     * @param status 比赛状态
     * @return 比赛列表
     */
    public List<Competition> getCompetitionsByStatus(String status) {
        return competitionMapper.findByStatus(status);
    }
    
    /**
     * 获取当前用户创建的比赛列表
     * 
     * @return 比赛列表
     */
    public List<Competition> getMyCreatedCompetitions() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询用户创建的比赛
        return competitionMapper.findByCreatedBy(userId);
    }
    
    /**
     * 获取比赛列表并附加当前用户的报名状态
     * 
     * @param competitions 比赛列表
     * @return 包含报名状态的比赛列表
     */
    public List<Map<String, Object>> getCompetitionsWithEnrollmentStatus(List<Competition> competitions) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 尝试获取当前用户的运动员信息
        Long athleteId = null;
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Athlete athlete = athleteMapper.findByUserId(userId);
            if (athlete != null) {
                athleteId = athlete.getId();
            }
        } catch (Exception e) {
            // 未登录或无运动员信息，athleteId 保持为 null
        }
        
        // 为每个比赛添加报名状态
        for (Competition competition : competitions) {
            Map<String, Object> competitionMap = new HashMap<>();
            competitionMap.put("id", competition.getId());
            competitionMap.put("name", competition.getName());
            competitionMap.put("roundsCount", competition.getRoundsCount());
            competitionMap.put("shotsPerRound", competition.getShotsPerRound());
            competitionMap.put("timeLimitPerShot", competition.getTimeLimitPerShot());
            competitionMap.put("status", competition.getStatus());
            competitionMap.put("createdBy", competition.getCreatedBy());
            competitionMap.put("createdAt", competition.getCreatedAt());
            competitionMap.put("startedAt", competition.getStartedAt());
            competitionMap.put("endedAt", competition.getEndedAt());
            competitionMap.put("completedAt", competition.getCompletedAt());
            competitionMap.put("durationSeconds", competition.getDurationSeconds());
            
            // 检查是否已报名
            boolean isEnrolled = false;
            if (athleteId != null) {
                CompetitionAthlete enrollment = competitionAthleteMapper.findByCompetitionIdAndAthleteId(
                    competition.getId(), athleteId);
                isEnrolled = (enrollment != null && "ENROLLED".equals(enrollment.getStatus()));
            }
            competitionMap.put("isEnrolled", isEnrolled);
            
            result.add(competitionMap);
        }
        
        return result;
    }
} 
