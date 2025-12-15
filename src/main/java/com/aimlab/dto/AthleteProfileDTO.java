package com.aimlab.dto;

import com.aimlab.entity.Athlete;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 运动员个人资料数据传输对象
 * 包含运动员基本信息、历史记录和生涯统计数据
 */
@Data
public class AthleteProfileDTO {
    
    /**
     * 运动员ID
     */
    private Long id;
    
    /**
     * 关联的用户ID
     */
    private Long userId;
    
    /**
     * 运动员姓名
     */
    private String name;
    
    /**
     * 性别：MALE(男), FEMALE(女), UNKNOWN(未知)
     */
    private String gender;
    
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    
    /**
     * 等级
     */
    private String level;
    
    /**
     * 是否有头像
     */
    private Boolean hasAvatar;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 历史记录列表（训练和比赛）
     */
    private List<AthleteHistoryItemDTO> historyItems;
    
    /**
     * 生涯总射击次数
     */
    private Integer careerTotalShots;
    
    /**
     * 生涯平均环数
     */
    private BigDecimal careerAverageScore;
    
    /**
     * 生涯最高环数
     */
    private BigDecimal careerBestScore;
    
    /**
     * 参加的比赛总数
     */
    private Integer totalCompetitions;
    
    /**
     * 获得第一名的次数
     */
    private Integer competitionsWon;
    
    /**
     * 获得前三名的次数
     */
    private Integer competitionsTopThree;
    
    /**
     * 训练场次总数
     */
    private Integer totalTrainingSessions;
    
    /**
     * 总训练时长（分钟）
     */
    private Long totalTrainingMinutes;
    
    /**
     * 从Athlete实体构建基本信息
     * 
     * @param athlete 运动员实体
     * @return AthleteProfileDTO对象
     */
    public static AthleteProfileDTO fromAthlete(Athlete athlete) {
        AthleteProfileDTO profileDTO = new AthleteProfileDTO();
        profileDTO.setId(athlete.getId());
        profileDTO.setUserId(athlete.getUserId());
        profileDTO.setName(athlete.getName());
        profileDTO.setGender(athlete.getGender());
        profileDTO.setBirthDate(athlete.getBirthDate());
        profileDTO.setLevel(athlete.getLevel());
        profileDTO.setHasAvatar(athlete.isHasAvatar());
        profileDTO.setCreatedAt(athlete.getCreatedAt());
        profileDTO.setUpdatedAt(athlete.getUpdatedAt());
        return profileDTO;
    }
} 