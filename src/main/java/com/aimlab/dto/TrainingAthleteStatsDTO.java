package com.aimlab.dto;

import java.math.BigDecimal;

/**
 * 运动员维度训练统计
 */
public class TrainingAthleteStatsDTO {
    private Long athleteId;
    private String athleteName;
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal stabilityIndex;
    private Long totalShots;
    private Long sessionCount;

    public Long getAthleteId() {
        return athleteId;
    }

    public void setAthleteId(Long athleteId) {
        this.athleteId = athleteId;
    }

    public String getAthleteName() {
        return athleteName;
    }

    public void setAthleteName(String athleteName) {
        this.athleteName = athleteName;
    }

    public BigDecimal getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(BigDecimal avgScore) {
        this.avgScore = avgScore;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public BigDecimal getMinScore() {
        return minScore;
    }

    public void setMinScore(BigDecimal minScore) {
        this.minScore = minScore;
    }

    public BigDecimal getStabilityIndex() {
        return stabilityIndex;
    }

    public void setStabilityIndex(BigDecimal stabilityIndex) {
        this.stabilityIndex = stabilityIndex;
    }

    public Long getTotalShots() {
        return totalShots;
    }

    public void setTotalShots(Long totalShots) {
        this.totalShots = totalShots;
    }

    public Long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(Long sessionCount) {
        this.sessionCount = sessionCount;
    }
}
