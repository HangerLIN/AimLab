package com.aimlab.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aimlab.entity.Athlete;
import com.aimlab.entity.ShootingRecord;
import com.aimlab.service.AthleteService;
import com.aimlab.service.CompetitionService;
import com.aimlab.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * WebSocket消息处理控制器
 */
@Controller
public class WebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    @Autowired
    private CompetitionService competitionService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private AthleteService athleteService;
    
    /**
     * 处理比赛射击记录
     * 
     * @param record 射击记录
     * @return 处理结果
     */
    @MessageMapping("/competition/shot")
    public void handleCompetitionShot(ShootingRecord record) {
        try {
            logger.info("收到比赛射击记录: 比赛ID={}, 用户ID={}, 运动员ID={}, 得分={}", 
                       record.getCompetitionId(), record.getUserId(), record.getAthleteId(), record.getScore());
            
            // 如果前端没有设置 athleteId，需要根据 userId 查询
            if (record.getAthleteId() == null) {
                Long userId = record.getUserId();
                
                // 如果 userId 也没有，尝试从 Sa-Token 获取
                if (userId == null) {
                    try {
                        userId = StpUtil.getLoginIdAsLong();
                        logger.info("从Sa-Token获取用户ID: {}", userId);
                    } catch (Exception e) {
                        logger.warn("无法从Sa-Token获取用户ID（WebSocket上下文）: {}", e.getMessage());
                    }
                }
                
                // 必须有 userId 才能继续
                if (userId == null) {
                    logger.error("无法获取用户ID，拒绝记录");
                    webSocketService.sendCustomMessage(
                        String.valueOf(record.getCompetitionId()),
                        "error",
                        "无法确认用户身份，请重新登录"
                    );
                    return;
                }
                
                // 根据用户ID获取运动员信息
                Athlete athlete = athleteService.getAthleteByUserId(userId);
                if (athlete == null) {
                    logger.error("用户ID {} 没有对应的运动员档案", userId);
                    webSocketService.sendCustomMessage(
                        String.valueOf(record.getCompetitionId()),
                        "error",
                        "您还没有创建运动员档案，请先创建档案"
                    );
                    return;
                }
                
                // 设置运动员ID和用户ID
                record.setAthleteId(athlete.getId());
                record.setUserId(userId);
                logger.info("✓ 从用户ID {} 获取运动员ID: {}", userId, athlete.getId());
            } else {
                logger.info("✓ 使用前端提供的运动员ID: {}", record.getAthleteId());
            }
            
            // 设置记录时间
            record.setShotAt(LocalDateTime.now());
            record.setRecordType("COMPETITION");
            
            // 保存射击记录
            ShootingRecord savedRecord = competitionService.addCompetitionRecord(record);
            
            // 通过WebSocket广播给所有客户端
            webSocketService.sendShootingRecord(
                String.valueOf(record.getCompetitionId()), 
                savedRecord
            );
            
            // 获取并广播最新排名
            try {
                webSocketService.sendRankingUpdate(
                    String.valueOf(record.getCompetitionId()),
                    competitionService.getLiveRanking(record.getCompetitionId())
                );
            } catch (Exception e) {
                logger.warn("更新实时排名失败: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("处理比赛射击记录失败: {}", e.getMessage(), e);
            
            // 发送错误消息给客户端
            webSocketService.sendCustomMessage(
                String.valueOf(record.getCompetitionId()),
                "error",
                "射击记录失败: " + e.getMessage()
            );
        }
    }
} 