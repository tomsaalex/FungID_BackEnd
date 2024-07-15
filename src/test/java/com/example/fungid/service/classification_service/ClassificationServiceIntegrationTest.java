package com.example.fungid.service.classification_service;

import com.example.fungid.config.TestFileSystemConfig;
import com.example.fungid.config.TestRestTemplateConfig;
import com.example.fungid.domain.User;
import com.example.fungid.repository.ClassificationRepository;
import com.example.fungid.repository.ImageRepository;
import com.example.fungid.service.ClassificationService;
import com.example.fungid.service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

//@SpringBootTest
@ContextConfiguration(classes = {TestRestTemplateConfig.class, TestFileSystemConfig.class})
@ActiveProfiles("test")
@DataJpaTest
@Import({TestFileSystemConfig.class, ImageRepository.class, NetworkService.class, ClassificationService.class})

class ClassificationServiceIntegrationTest {
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClassificationRepository classificationRepository;
    @Autowired
    private ClassificationService classificationService;

    private User existingUser;

    /*@BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("user1");
        existingUser.setEmail("user1@gmail.com");
        existingUser.setPassword("password1");
    }

    @AfterEach
    void tearDown() {
        existingUser = null;
    }

    @Test
    void test_classifyMushroom_classificationSucceeded() {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile("mushroomImage", "mushroom.jpg", "image/jpeg", "mushroom".getBytes());
        String expectedClassificationResult = "random_result";

        Mockito.when(restTemplate.postForObject(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(
                new ResponseEntity<>(
                        new ClassificationResultAI(expectedClassificationResult), null, 200)
        );

        // Act
        MushroomClassificationDTO classificationResult = classificationService.classifyMushroom(existingUser, imageFile, LocalDateTime.now());

        // Assert
        assertNotNull(classificationResult);
        assertEquals(expectedClassificationResult, classificationResult.classificationResult);

        Mockito.verify(restTemplate, Mockito.times(1)).postForObject(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void getImage() {
    }

    @Test
    void getAllMushroomInstancesForUser() {
    }

    @Test
    void getMushroomInstanceForUser() {
    }

    @Test
    void mapToDTO() {
    }*/
}