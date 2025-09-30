package com.aimlab.config;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket配置类 - 使用STOMP协议
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单代理，前缀为 /topic
        config.enableSimpleBroker("/topic");
        // 客户端发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，并启用SockJS支持
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new SaTokenHandshakeInterceptor())
                .withSockJS();
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new SaTokenChannelInterceptor());
    }
    
    /**
     * Sa-Token握手拦截器
     */
    private static class SaTokenHandshakeInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                      WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            
            // 从请求中获取token
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                String token = servletRequest.getServletRequest().getParameter("token");
                
                if (token == null || token.isEmpty()) {
                    token = servletRequest.getServletRequest().getHeader("aimlab-token");
                }
                
                if (token != null && !token.isEmpty()) {
                    attributes.put("aimlab-token", token);
                    logger.debug("WebSocket握手: 获取到token");
                } else {
                    logger.warn("WebSocket握手: 未找到token");
                }
            }
            
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Exception exception) {
        }
    }
    
    /**
     * Sa-Token消息通道拦截器
     */
    private static class SaTokenChannelInterceptor implements ChannelInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            
            if (accessor != null && accessor.getSessionAttributes() != null) {
                String token = (String) accessor.getSessionAttributes().get("aimlab-token");
                
                if (token != null && !token.isEmpty()) {
                    try {
                        // 在当前线程中设置Sa-Token的上下文
                        StpUtil.getLoginIdByToken(token);
                        logger.debug("WebSocket消息: 设置token上下文成功");
                    } catch (Exception e) {
                        logger.warn("WebSocket消息: token验证失败: {}", e.getMessage());
                    }
                }
            }
            
            return message;
        }
    }
    
    /**
     * 配置ObjectMapper Bean，支持Java 8日期时间类型
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册Java 8日期时间模块
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        // 禁用将日期写为时间戳
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
} 