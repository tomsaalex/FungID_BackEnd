package com.example.fungid.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {

    private final String AI_MODEL_ADDRESS = ""; // TODO Add actual model address

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(AI_MODEL_ADDRESS)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}