package com.aimlab.dto;

import java.math.BigDecimal;

/**
 * 项目维度训练统计
 */
public class TrainingProjectStatsDTO {
    private String projectType;
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal stabilityIndex;
    private Long totalShots;
    private Long sessionCount;

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
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
