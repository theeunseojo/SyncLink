package com.SyncLink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SyncLink API Document")
                        .version("1.0.0")
                        .description("언제비어(SyncLink) 프로젝트의 API 명세서입니다."));
    }
}