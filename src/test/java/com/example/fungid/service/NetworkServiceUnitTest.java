package com.example.fungid.service;

import com.example.fungid.dto.ClassificationResultAI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class NetworkServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NetworkService networkService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        networkService = null;
    }

    @Test
    void test_classifyMushroomWithAIModel_classificationSucceeded() {
        // Arrange
        FileSystemResource image = new FileSystemResource("test_image.jpg");
        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(
                new ResponseEntity<>(new ClassificationResultAI("random_result"), null, 200));

        // Act
        String classificationResult = assertDoesNotThrow(() -> networkService.classifyMushroomWithAIModel(image));

        // Assert
        assertNotNull(classificationResult);
        assertNotEquals(classificationResult.length(), 0);
    }

    @Test
    void test_classifyMushroomWithAIModel_classificationFailed() {
        // Arrange
        FileSystemResource image = new FileSystemResource("");
        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(
                new ResponseEntity<>(new ClassificationResultAI("No selected file"), null, 400));

        // Act
        String classificationResult = networkService.classifyMushroomWithAIModel(image);

        // Assert
        assertNotNull(classificationResult);
        assertEquals(classificationResult, "No selected file");
    }

    @Test
    void test_classifyMushroomWithAIModel_connectionRefused() {
        // Arrange
        FileSystemResource image = new FileSystemResource("test_image.jpg");
        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenThrow(new ResourceAccessException("Connection refused"));

        // Act
        assertThrows(ResourceAccessException.class,() -> networkService.classifyMushroomWithAIModel(image));
    }
}