package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Spring Boot REST API")
                .version("1.0.0")
                .description("Spring Boot REST API with CRUD operations for User management")
                .contact(new Contact()
                    .name("Developer")
                    .email("developer@example.com")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Development Server")
            ));
    }
}