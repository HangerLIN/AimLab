package com.aimlab.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对比分析请求DTO
 */
@Data
public class CompareRequestDTO {
    private List<Long> athleteIds;
    private String level;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String projectType;
    private Integer maxCount;
}
