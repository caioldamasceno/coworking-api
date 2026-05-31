package com.coworking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI coworkingOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Coworking API")
                .description("API REST para gestao de reservas de salas e auditorios de um coworking.")
                .version("1.0.0"));
    }
}
