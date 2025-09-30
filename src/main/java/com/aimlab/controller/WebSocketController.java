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
            logger.info("收到比赛射击记录: 比赛ID={}, 得分={}", 
                       record.getCompetitionId(), record.getScore());
            
            // 从Sa-Token获取当前登录用户的ID
            try {
                Long userId = StpUtil.getLoginIdAsLong();
                
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
                
                // 设置运动员ID
                record.setAthleteId(athlete.getId());
                logger.info("设置运动员ID: {}, 用户ID: {}", athlete.getId(), userId);
                
            } catch (Exception e) {
                logger.error("获取用户信息失败: {}", e.getMessage(), e);
                webSocketService.sendCustomMessage(
                    String.valueOf(record.getCompetitionId()),
                    "error",
                    "请先登录后再进行射击"
                );
                return;
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