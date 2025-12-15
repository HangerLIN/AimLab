package com.aimlab.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 运动员对比数据DTO
 */
@Data
public class AthleteCompareDTO {
    private Long athleteId;
    private String athleteName;
    private String athleteLevel;
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal stabilityIndex;
    private Long totalShots;
    private Long sessionCount;
    private BigDecimal perfectRate;
    private BigDecimal highScoreRate;
    private BigDecimal stabilityScore;
}
