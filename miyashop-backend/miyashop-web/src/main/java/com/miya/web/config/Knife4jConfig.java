package com.miya.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MiyaShop电商系统API")
                        .version("1.0.0")
                        .description("女装电商网站后端接口文档")
                        .contact(new Contact()
                                .name("MiyaShop")
                                .email("support@miyashop.com")));
    }
}