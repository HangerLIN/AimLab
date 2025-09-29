package com.aimlab.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 比赛WebSocket服务端点
 */
@ServerEndpoint("/ws/competition/{competitionId}")
@Component
public class CompetitionWebSocketServer {
    
    private static final Logger logger = LoggerFactory.getLogger(CompetitionWebSocketServer.class);
    
    /**
     * 存储比赛ID和对应的客户端会话集合
     * 一个比赛可能对应多个客户端会话
     */
    private static final Map<String, Set<Session>> competitionSessions = new ConcurrentHashMap<>();
    
    /**
     * 当客户端连接建立时调用
     * 
     * @param session 客户端会话
     * @param competitionId 比赛ID
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("competitionId") String competitionId) {
        // 获取该比赛ID对应的会话集合，如果不存在则创建新的集合
        Set<Session> sessions = competitionSessions.computeIfAbsent(competitionId, k -> new CopyOnWriteArraySet<>());
        
        // 将新的会话添加到集合中
        sessions.add(session);
        
        logger.info("新的WebSocket连接已建立，比赛ID: {}, 会话ID: {}, 当前连接数: {}", 
                competitionId, session.getId(), sessions.size());
    }
    
    /**
     * 当客户端连接关闭时调用
     * 
     * @param session 客户端会话
     * @param competitionId 比赛ID
     */
    @OnClose
    public void onClose(Session session, @PathParam("competitionId") String competitionId) {
        // 获取该比赛ID对应的会话集合
        Set<Session> sessions = competitionSessions.get(competitionId);
        
        if (sessions != null) {
            // 从集合中移除关闭的会话
            sessions.remove(session);
            
            // 如果集合为空，从Map中移除该比赛ID的条目
            if (sessions.isEmpty()) {
                competitionSessions.remove(competitionId);
            }
            
            logger.info("WebSocket连接已关闭，比赛ID: {}, 会话ID: {}, 剩余连接数: {}", 
                    competitionId, session.getId(), sessions.size());
        }
    }
    
    /**
     * 当收到客户端消息时调用
     * 
     * @param message 客户端发送的消息
     * @param session 客户端会话
     * @param competitionId 比赛ID
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("competitionId") String competitionId) {
        logger.debug("收到来自比赛ID: {} 的消息: {}", competitionId, message);
        // 在本应用中，我们主要是服务器向客户端推送消息，所以这里不做特殊处理
    }
    
    /**
     * 当发生错误时调用
     * 
     * @param session 客户端会话
     * @param error 异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("WebSocket发生错误，会话ID: {}, 错误信息: {}", session.getId(), error.getMessage(), error);
    }
    
    /**
     * 向指定比赛的所有连接客户端广播消息
     * 
     * @param competitionId 比赛ID
     * @param message 要广播的消息
     */
    public void broadcastToCompetition(String competitionId, String message) {
        Set<Session> sessions = competitionSessions.get(competitionId);
        
        if (sessions != null && !sessions.isEmpty()) {
            logger.debug("向比赛ID: {} 的 {} 个客户端广播消息", competitionId, sessions.size());
            
            for (Session session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(message);
                    }
                } catch (IOException e) {
                    logger.error("向会话ID: {} 发送消息失败: {}", session.getId(), e.getMessage(), e);
                }
            }
        } else {
            logger.debug("比赛ID: {} 没有活跃的WebSocket连接", competitionId);
        }
    }
    
    /**
     * 获取指定比赛的连接数量
     * 
     * @param competitionId 比赛ID
     * @return 连接数量
     */
    public int getConnectionCount(String competitionId) {
        Set<Session> sessions = competitionSessions.get(competitionId);
        return sessions != null ? sessions.size() : 0;
    }
    
    /**
     * 获取所有比赛的连接总数
     * 
     * @return 连接总数
     */
    public int getTotalConnectionCount() {
        int count = 0;
        for (Set<Session> sessions : competitionSessions.values()) {
            count += sessions.size();
        }
        return count;
    }
} 