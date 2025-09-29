package com.aimlab.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j配置类
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 添加认证头配置
        Components components = new Components();
        components.addSecuritySchemes("aimlab-token", 
            new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("aimlab-token")
                .description("认证token"));

        // 添加认证需求
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList("aimlab-token");

        return new OpenAPI()
            .components(components)
            .addSecurityItem(securityRequirement)
            .info(new Info()
                .title("射击训练比赛系统 API文档")
                .description("射击训练比赛系统接口文档，提供训练和比赛相关的API")
                .version("1.0.0")
                .contact(new Contact()
                    .name("AimLab")
                    .email("support@aimlab.com")
                    .url("https://www.aimlab.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
} 