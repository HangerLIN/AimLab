package com.aimlab.dto;

import java.math.BigDecimal;

/**
 * 时间维度训练统计
 */
public class TrainingTimeStatsDTO {
    private String period;
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal stabilityIndex;
    private Long totalShots;
    private Long sessionCount;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
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
