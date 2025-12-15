package com.aimlab.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 趋势数据点DTO
 */
@Data
public class TrendDataDTO {
    private String period;
    private Integer weekNumber;
    private LocalDate weekStartDate;
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal stabilityIndex;
    private Long totalShots;
    private Long sessionCount;
    private BigDecimal movingAverage;
    private BigDecimal predictedScore;
    private Boolean isPredicted;
}
