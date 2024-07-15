package com.example.fungid.config;

import com.google.common.jimfs.Jimfs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.nio.file.FileSystem;
import java.nio.file.Path;

@TestConfiguration
//@Configuration
//@Profile("test")
public class TestFileSystemConfig {
    @Value("${file.base.path}")
    private String basePath;

    @Bean
    public FileSystem jimfsFileSystem() {
        return Jimfs.newFileSystem(com.google.common.jimfs.Configuration.windows());
    }

    @Bean
    public Path testBasePath(FileSystem jimfsFileSystem) {
        return jimfsFileSystem.getPath(basePath);
    }
}
