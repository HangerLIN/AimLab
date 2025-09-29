package com.aimlab.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.time.temporal.ChronoUnit;

/**
 * 比赛状态实体类，用于在内存中保存比赛实时状态
 */
@Data
public class CompetitionStatus {
    
    /**
     * 比赛ID
     */
    private Long competitionId;
    
    /**
     * 比赛开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 比赛暂停时间
     */
    private LocalDateTime pauseTime;
    
    /**
     * 比赛恢复时间
     */
    private LocalDateTime resumeTime;
    
    /**
     * 比赛当前状态
     * CREATED: 已创建
     * STARTED: 已开始
     * PAUSED: 已暂停
     * COMPLETED: 已完成
     */
    private String status;
    
    /**
     * 当前轮次
     */
    private Integer currentRound = 1;
    
    /**
     * 总暂停时长（秒）
     */
    private long totalPauseDurationSeconds = 0;
    
    /**
     * 运动员射击次数统计，key为运动员ID，value为射击次数
     */
    private Map<Long, Integer> athleteShotCounts = new ConcurrentHashMap<>();
    
    /**
     * 无参构造函数
     */
    public CompetitionStatus() {
        this.status = "CREATED";
        this.currentRound = 1;
    }
    
    /**
     * 带参构造函数
     * 
     * @param competitionId 比赛ID
     */
    public CompetitionStatus(Long competitionId) {
        this.competitionId = competitionId;
        this.status = "CREATED";
        this.startTime = LocalDateTime.now();
        this.athleteShotCounts = new ConcurrentHashMap<>();
    }
    
    /**
     * 记录射击，并返回当前射击次数
     * 
     * @param athleteId 运动员ID
     * @return 当前射击次数
     */
    public int recordShot(Long athleteId) {
        int currentCount = athleteShotCounts.getOrDefault(athleteId, 0) + 1;
        athleteShotCounts.put(athleteId, currentCount);
        return currentCount;
    }
    
    /**
     * 获取运动员射击次数
     * 
     * @param athleteId 运动员ID
     * @return 射击次数
     */
    public int getAthleteShotCount(Long athleteId) {
        return athleteShotCounts.getOrDefault(athleteId, 0);
    }
    
    /**
     * 获取所有运动员射击次数
     * 
     * @return 所有运动员射击次数
     */
    public Map<Long, Integer> getAllAthleteShotCounts() {
        return new HashMap<>(athleteShotCounts);
    }
    
    /**
     * 开始新一轮
     * 
     * @param roundNumber 轮次编号
     */
    public void startNewRound(int roundNumber) {
        this.currentRound = roundNumber;
        this.startTime = LocalDateTime.now();
        // 重置所有运动员的射击计数
        this.athleteShotCounts.clear();
    }
    
    /**
     * 结束当前轮次
     */
    public void endCurrentRound() {
        // 实现结束当前轮次的逻辑
    }
    
    /**
     * 暂停比赛
     */
    public void pause() {
        this.status = "PAUSED";
        this.pauseTime = LocalDateTime.now();
        // 计算暂停时长
        long pauseDuration = java.time.Duration.between(this.pauseTime, LocalDateTime.now()).getSeconds();
        this.totalPauseDurationSeconds += pauseDuration;
    }
    
    /**
     * 恢复比赛
     */
    public void resume() {
        this.status = "STARTED";
    }
    
    /**
     * 设置比赛恢复时间，并计算暂停时长
     * 
     * @param resumeTime 恢复时间
     */
    public void setResumeTime(LocalDateTime resumeTime) {
        this.resumeTime = resumeTime;
        
        // 计算暂停时长并累加
        if (this.pauseTime != null && resumeTime != null) {
            long pauseDurationSeconds = ChronoUnit.SECONDS.between(this.pauseTime, resumeTime);
            this.totalPauseDurationSeconds += pauseDurationSeconds;
            this.pauseTime = null;
        }
    }
    
    /**
     * 计算比赛总时长（秒），减去暂停时间
     * 
     * @return 比赛有效时长（秒）
     */
    public long calculateTotalDuration() {
        LocalDateTime endTime = LocalDateTime.now();
        
        // 计算总时长
        long totalDurationSeconds = ChronoUnit.SECONDS.between(this.startTime, endTime);
        
        // 减去暂停时长
        return totalDurationSeconds - this.totalPauseDurationSeconds;
    }
} 