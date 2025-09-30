package com.aimlab.controller;

import com.aimlab.entity.ShootingRecord;
import com.aimlab.service.CompetitionService;
import com.aimlab.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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