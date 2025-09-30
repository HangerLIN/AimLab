package com.aimlab.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册Sa-Token拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 公共接口列表
        List<String> publicPaths = Arrays.asList(
            "/api/users/login", 
            "/api/users/register", 
            "/api/auth/login",
            "/api/auth/register",
            "/api/competitions",
            "/api/competitions/*",
            "/api/competitions/*/status",
            "/api/competitions/*/rankings",
            "/api/competitions/*/results",
            "/api/competitions/*/start",
            "/api/competitions/*/pause",
            "/api/competitions/*/resume",
            "/api/competitions/*/complete",
            "/api/competitions/*/cancel",
            "/api/competitions/*/enroll",
            "/api/records/competition",
            "/api/records/competition/*",
            "/api/athletes/*/profile",
            "/api/training/sessions",
            "/api/training/sessions/*",
            "/error",
            "/actuator/health",
            // Knife4j相关路径
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/doc.html",
            "/webjars/**"
        );
        
        // 注册Sa-Token拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(publicPaths.toArray(new String[0]));
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
