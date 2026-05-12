package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        String jwtSchemeName = "JWT TOKEN";

        SecurityRequirement securityRequirement =
                new SecurityRequirement().addList(jwtSchemeName);

        SecurityScheme securityScheme =
                new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("JuCompany API")
                        .description("인력 매칭 플랫폼 API")
                        .version("v1"))
                .addSecurityItem(securityRequirement)
                .schemaRequirement(jwtSchemeName, securityScheme);
    }
}