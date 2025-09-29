package com.aimlab.websocket;

import com.aimlab.entity.ShootingRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private CompetitionWebSocketServer webSocketServer;
    
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
            
            // 将消息对象序列化为JSON字符串
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            // 广播消息
            webSocketServer.broadcastToCompetition(competitionId, jsonMessage);
            
            logger.debug("已向比赛ID: {} 发送射击记录, 记录ID: {}", competitionId, record.getId());
        } catch (JsonProcessingException e) {
            logger.error("序列化射击记录失败: {}", e.getMessage(), e);
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
            
            // 将消息对象序列化为JSON字符串
            String jsonMessage = objectMapper.writeValueAsString(messageObj);
            
            // 广播消息
            webSocketServer.broadcastToCompetition(competitionId, jsonMessage);
            
            logger.debug("已向比赛ID: {} 发送状态更新, 状态: {}", competitionId, status);
        } catch (JsonProcessingException e) {
            logger.error("序列化比赛状态更新失败: {}", e.getMessage(), e);
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
            
            // 将消息对象序列化为JSON字符串
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            // 广播消息
            webSocketServer.broadcastToCompetition(competitionId, jsonMessage);
            
            logger.debug("已向比赛ID: {} 发送排名更新", competitionId);
        } catch (JsonProcessingException e) {
            logger.error("序列化排名更新失败: {}", e.getMessage(), e);
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
            
            // 将消息对象序列化为JSON字符串
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            // 广播消息
            webSocketServer.broadcastToCompetition(competitionId, jsonMessage);
            
            logger.debug("已向比赛ID: {} 发送自定义消息, 类型: {}", competitionId, type);
        } catch (JsonProcessingException e) {
            logger.error("序列化自定义消息失败: {}", e.getMessage(), e);
        }
    }
} 