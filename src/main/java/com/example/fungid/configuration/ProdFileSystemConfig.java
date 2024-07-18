package com.example.fungid.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Profile("!test")
public class ProdFileSystemConfig {

    @Value("${file.base.path}")
    private String basePath;

    @Bean
    public Path prodBasePath() {
        return Paths.get(basePath);
    }
}
