package com.example;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI evsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EVS Demo API")
                        .description("Entity Variable Storage - API tekshirish uchun")
                        .version("1.0.0"));
    }
}
