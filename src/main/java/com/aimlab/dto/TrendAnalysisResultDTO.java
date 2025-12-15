package com.aimlab.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 趋势分析结果DTO
 */
@Data
public class TrendAnalysisResultDTO {
    private Long athleteId;
    private String athleteName;
    private Integer analyzedWeeks;
    private List<TrendDataDTO> trendData;
    private String trendDirection;
    private BigDecimal trendSlope;
    private List<TrendDataDTO> predictions;
    private TrendSummary summary;

    @Data
    public static class TrendSummary {
        private BigDecimal periodHighest;
        private BigDecimal periodLowest;
        private BigDecimal periodAverage;
        private BigDecimal improvement;
        private String bestWeek;
        private String worstWeek;
    }
}
