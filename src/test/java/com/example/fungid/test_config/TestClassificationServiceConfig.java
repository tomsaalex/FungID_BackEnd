package com.example.fungid.test_config;

import com.example.fungid.repository.ClassificationRepository;
import com.example.fungid.repository.ImageRepository;
import com.example.fungid.service.ClassificationService;
import com.example.fungid.service.NetworkService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestClassificationServiceConfig {
    @Primary
    @Bean
    public ClassificationService classificationService(NetworkService networkService, ClassificationRepository classificationRepository, ImageRepository imageRepository) {
        return new ClassificationService(networkService, classificationRepository, imageRepository);
    }
}
