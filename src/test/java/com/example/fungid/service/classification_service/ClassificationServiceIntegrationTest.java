package com.example.fungid.service.classification_service;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import com.example.fungid.dto.ClassificationResultAI;
import com.example.fungid.dto.MushroomClassificationDTO;
import com.example.fungid.repository.ClassificationRepository;
import com.example.fungid.repository.ImageRepository;
import com.example.fungid.repository.UserRepository;
import com.example.fungid.service.ClassificationService;
import com.example.fungid.test_config.TestClassificationServiceConfig;
import com.example.fungid.test_config.TestFileSystemConfig;
import com.example.fungid.test_config.TestRestTemplateConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

//@SpringBootTest
@ContextConfiguration(classes = {TestRestTemplateConfig.class, TestFileSystemConfig.class, TestClassificationServiceConfig.class})
@DataJpaTest
@Import({TestFileSystemConfig.class, TestRestTemplateConfig.class, TestClassificationServiceConfig.class, ImageRepository.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class ClassificationServiceIntegrationTest {
    @Autowired
    private ImageRepository imageRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ClassificationRepository classificationRepository;
    @Autowired
    private ClassificationService classificationService;

    @Autowired
    private UserRepository userRepository;
    private User existingUser;
    private List<MushroomInstance> existingMushroomInstances;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("user1");
        existingUser.setEmail("user1@gmail.com");
        existingUser.setPassword("password1");

        userRepository.saveAndFlush(existingUser);

        existingMushroomInstances = List.of(
                new MushroomInstance(existingUser, "mushroom1", "imageName1", LocalDateTime.now()),
                new MushroomInstance(existingUser, "mushroom2", "imageName2", LocalDateTime.now()),
                new MushroomInstance(existingUser, "mushroom3", "imageName3", LocalDateTime.now())
        );
        classificationRepository.saveAll(existingMushroomInstances);
    }

    @AfterEach
    void tearDown() {
        classificationRepository.deleteAll();
        userRepository.deleteAll();
        existingUser = null;
    }

    @Test
    void test_classifyMushroom_classificationSucceeded() {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", "mushroom".getBytes());
        String expectedClassificationResult = "random_result";

        ClassificationResultAI expectedResult = new ClassificationResultAI(expectedClassificationResult);
        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(
               new ResponseEntity<>(expectedResult, null, 200)
        );

        // Act
        MushroomClassificationDTO classificationResult = classificationService.classifyMushroom(existingUser, imageFile, LocalDateTime.now());

        // Assert
        assertNotNull(classificationResult);
        assertEquals(expectedClassificationResult, classificationResult.classificationResult);

        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void test_classifyMushroom_connectionRefused() {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", "mushroom".getBytes());
        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any())).thenThrow(new ResourceAccessException("Connection refused"));

        // Act
        assertThrows(ResourceAccessException.class, () -> classificationService.classifyMushroom(existingUser, imageFile, LocalDateTime.now()));

        // Assert
        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void test_getImage_imageFound() throws IOException {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", "mushroom".getBytes());
        Path imagePath = imageRepository.saveImageToStorage(existingUser.getId().toString(), imageFile);
        String imageName = imagePath.getFileName().toString();

        // Act
        byte[] image = classificationService.getImage(existingUser.getId(), imageName);

        // Assert
        assertNotNull(image);
        assertNotEquals(0, image.length);
        Files.deleteIfExists(imagePath);
    }

    @Test
    void getAllMushroomInstancesForUser() {
        // Act
        List<MushroomClassificationDTO> mushroomInstances = classificationService.getAllMushroomInstancesForUser(existingUser);

        // Assert
        assertNotNull(mushroomInstances);
        assertEquals(existingMushroomInstances.size(), mushroomInstances.size());
        for (int i = 0; i < existingMushroomInstances.size(); i++) {
            assertEquals(existingMushroomInstances.get(i).getClassificationResult(), mushroomInstances.get(i).classificationResult);
            assertEquals(existingMushroomInstances.get(i).getId(), mushroomInstances.get(i).mushroomInstanceId);
        }
    }

    @Test
    void getMushroomInstanceForUser() {
        // Arrange
        MushroomInstance expectedMushroomInstance = existingMushroomInstances.get(0);

        // Act
        MushroomInstance foundMushroom = classificationService.getMushroomInstanceForUser(expectedMushroomInstance.getId(), existingUser.getId());

        // Assert
        assertNotNull(foundMushroom);
        assertEquals(expectedMushroomInstance, foundMushroom);
    }
}