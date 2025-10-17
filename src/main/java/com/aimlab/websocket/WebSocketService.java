package com.aimlab.websocket;

import com.aimlab.entity.ShootingRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务类，提供WebSocket消息发送的业务接口
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 发送射击记录到指定比赛的所有连接客户端
     * 
     * @param competitionId 比赛ID
     * @param record 射击记录
     */
    public void sendShootingRecord(String competitionId, ShootingRecord record) {
        try {
            // 创建消息对象
            Map<String, Object> message = new HashMap<>();
            message.put("type", "SHOOTING_RECORD");
            message.put("data", record);
            
            // 发送消息到指定主题
            String destination = "/topic/competition/" + competitionId;
            messagingTemplate.convertAndSend(destination, message);
            
            logger.info("✓ 广播射击记录 - 比赛ID: {}, 运动员ID: {}, 得分: {}, 目标主题: {}", 
                       competitionId, record.getAthleteId(), record.getScore(), destination);
        } catch (Exception e) {
            logger.error("❌ 发送射击记录失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 发送比赛状态更新消息
     * 
     * @param competitionId 比赛ID
     * @param status 比赛状态
     * @param message 状态描述
     */
    public void sendCompetitionStatusUpdate(String competitionId, String status, String message) {
        try {
            // 创建消息对象
            Map<String, Object> messageObj = new HashMap<>();
            messageObj.put("type", "COMPETITION_STATUS");
            
            Map<String, String> data = new HashMap<>();
            data.put("status", status);
            data.put("message", message);
            
            messageObj.put("data", data);
            
            // 发送消息到指定主题
            String destination = "/topic/competition/" + competitionId + "/status";
            messagingTemplate.convertAndSend(destination, messageObj);
            
            logger.debug("已向比赛ID: {} 发送状态更新, 状态: {}", competitionId, status);
        } catch (Exception e) {
            logger.error("发送比赛状态更新失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 发送排名更新消息
     * 
     * @param competitionId 比赛ID
     * @param rankings 排名数据
     */
    public void sendRankingUpdate(String competitionId, Object rankings) {
        try {
            // 创建消息对象
            Map<String, Object> message = new HashMap<>();
            message.put("type", "RANKING_UPDATE");
            message.put("data", rankings);
            
            // 发送消息到指定主题
            String destination = "/topic/competition/" + competitionId;
            messagingTemplate.convertAndSend(destination, message);
            
            logger.info("✓ 广播排名更新 - 比赛ID: {}, 目标主题: {}", competitionId, destination);
        } catch (Exception e) {
            logger.error("❌ 发送排名更新失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 发送自定义消息
     * 
     * @param competitionId 比赛ID
     * @param type 消息类型
     * @param data 消息数据
     */
    public void sendCustomMessage(String competitionId, String type, Object data) {
        try {
            // 创建消息对象
            Map<String, Object> message = new HashMap<>();
            message.put("type", type);
            message.put("data", data);
            
            // 发送消息到指定主题
            String destination = "/topic/competition/" + competitionId;
            messagingTemplate.convertAndSend(destination, message);
            
            logger.debug("已向比赛ID: {} 发送自定义消息, 类型: {}", competitionId, type);
        } catch (Exception e) {
            logger.error("发送自定义消息失败: {}", e.getMessage(), e);
        }
    }
} 