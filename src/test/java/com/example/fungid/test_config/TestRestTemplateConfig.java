package com.example.fungid.test_config;

import com.example.fungid.service.NetworkService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
@Profile("test")
public class TestRestTemplateConfig {

    @Primary
    @Bean
    public RestTemplate restTemplate() {
        return Mockito.mock(RestTemplate.class);
        //return new RestTemplate();
    }

    @Primary
    @Bean
    public NetworkService networkService(RestTemplate restTemplate) {
        //assertTrue(Mockito.mockingDetails(restTemplate).isMock());
        return new NetworkService(restTemplate);
    }
}
