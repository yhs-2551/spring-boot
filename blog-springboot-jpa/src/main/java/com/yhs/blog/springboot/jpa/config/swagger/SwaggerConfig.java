package com.yhs.blog.springboot.jpa.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",  // "bearer-key"에서 "bearerAuth"로 변경
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement()  // 이 부분 추가
                        .addList("bearerAuth")) // SecurityScheme 이름과 일치
                .info(new Info()
                        .title("Blog API")
                        .version("1.0.0")  // 버전 업데이트
                        .description("Blog API Documentation - November 2024"));
    }
}