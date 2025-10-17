package com.aimlab.dto;

import com.aimlab.entity.ShootingRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 射击记录响应DTO
 */
@Data
public class ShootingRecordDTO {

    private Long id;
    private String recordType;
    private Long athleteId;
    private Integer competitionId;
    private Long trainingSessionId;
    private Integer roundNumber;
    private Integer shotNumber;
    private double x;
    private double y;
    private double score;
    private LocalDateTime shotAt;
    private String timestamp;

    /**
     * 从实体对象构建DTO
     *
     * @param record 射击记录实体
     * @return 射击记录DTO
     */
    public static ShootingRecordDTO fromEntity(ShootingRecord record) {
        ShootingRecordDTO dto = new ShootingRecordDTO();
        dto.setId(record.getId());
        dto.setRecordType(record.getRecordType());
        dto.setAthleteId(record.getAthleteId());
        dto.setCompetitionId(record.getCompetitionId());
        dto.setTrainingSessionId(record.getTrainingSessionId());
        dto.setRoundNumber(record.getRoundNumber());
        dto.setShotNumber(record.getShotNumber());
        dto.setX(toDouble(record.getX()));
        dto.setY(toDouble(record.getY()));
        dto.setScore(toDouble(record.getScore()));
        dto.setShotAt(record.getShotAt());
        dto.setTimestamp(record.getShotAt() != null ? record.getShotAt().toString() : null);
        return dto;
    }

    private static double toDouble(BigDecimal value) {
        return value == null ? 0D : value.doubleValue();
    }
}
