package com.aimlab.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token配置类
 * 
 * 权限控制策略：
 * 1. 公开接口：登录、注册、健康检查等
 * 2. GET请求公开：比赛列表、运动员信息等查询接口
 * 3. 写操作需登录：POST/PUT/DELETE 需要登录验证
 * 4. 管理员接口：需要登录 + 管理员权限
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册Sa-Token拦截器，实现细粒度权限控制
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 使用 Sa-Token 路由匹配进行权限控制
            SaRouter
                // 排除公开接口
                .notMatch(
                    "/api/users/login",
                    "/api/users/register",
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/test",
                    "/api/admin/settings/public/**",
                    "/ws/**",
                    "/error",
                    "/actuator/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/doc.html",
                    "/webjars/**"
                )
                // 对于数据查询接口，仅GET请求公开
                .match("/api/competitions/**", "/api/athletes/**", 
                       "/api/training-sessions/**", "/api/shooting-records/**",
                       "/api/training-analytics/**")
                .notMatchMethod("GET")  // 非GET请求需要登录
                .check(r -> StpUtil.checkLogin());
            
            // 其他接口（如 /api/admin/**）需要登录，但排除公开接口
            SaRouter
                .match("/api/admin/**")
                .notMatch("/api/admin/settings/public/**")
                .check(r -> StpUtil.checkLogin());
            
            // 训练接口需要登录
            SaRouter
                .match("/api/training/**")
                .check(r -> StpUtil.checkLogin());
            
            // 用户信息接口需要登录
            SaRouter
                .match("/api/users/me")
                .check(r -> StpUtil.checkLogin());
            
            // 站内信接口需要登录
            SaRouter
                .match("/api/messages/**")
                .check(r -> StpUtil.checkLogin());
                
        })).addPathPatterns("/**");
    }
    
    /**
     * 配置CORS跨域支持
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:5173", 
                    "http://127.0.0.1:5173",
                    "http://localhost:5174", 
                    "http://127.0.0.1:5174"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
