package com.cabinet360.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CoreServiceConfig {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Bean
    public WebClient authServiceWebClient() {
        System.out.println("Auth service URL: " + authServiceUrl);
        return WebClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }
}
