package com.example.fungid.service.classification_service;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import com.example.fungid.dto.MushroomClassificationDTO;
import com.example.fungid.exceptions.mushroom_id.*;
import com.example.fungid.repository.ClassificationRepository;
import com.example.fungid.repository.ImageRepository;
import com.example.fungid.service.ClassificationService;
import com.example.fungid.service.NetworkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({ImageRepository.class, MushroomClassificationDTO.class})
class ClassificationServiceUnitTest {

    @InjectMocks
    private ClassificationService classificationService;

    @Mock
    private ClassificationRepository classificationRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private NetworkService networkService;

    User user;
    byte[] validImageContent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("test_username", "test_email", "test_password");
        user.setId(1L);
        validImageContent = "test_image".getBytes();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Tag("Unit_Testing")
    void test_classifyMushroom_validImage() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test_image.jpg", "image/jpeg", validImageContent);
        Path validPath = Path.of("valid_path");
        String validClassificationResult = "valid_classification_result";

        MushroomInstance instanceToSave = new MushroomInstance(user, validClassificationResult, "test_image.jpg", LocalDateTime.now());

        Mockito.when(imageRepository.saveImageToStorage(user.getId().toString(), file)).thenReturn(validPath);
        Mockito.when(imageRepository.getImageAsResource(validPath)).thenReturn(new FileSystemResource(validPath));
        Mockito.when(networkService.classifyMushroomWithAIModel(new FileSystemResource(validPath))).thenReturn(validClassificationResult);
        Mockito.when(classificationRepository.save(Mockito.any())).thenReturn(instanceToSave);

        // Act
        MushroomClassificationDTO savedMushroomInstance = assertDoesNotThrow(() -> classificationService.classifyMushroom(user, file, LocalDateTime.now()));

        // Assert
        assertNotNull(savedMushroomInstance);
        assertEquals(validClassificationResult, savedMushroomInstance.classificationResult);

        Mockito.verify(imageRepository, Mockito.times(1)).saveImageToStorage(user.getId().toString(), file);
        Mockito.verify(imageRepository, Mockito.times(1)).getImageAsResource(validPath);
        Mockito.verify(networkService, Mockito.times(1)).classifyMushroomWithAIModel(Mockito.any());
        Mockito.verify(classificationRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    @Tag("Unit_Testing")
    void test_classifyMushroom_emptyFile() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test_image.jpg", "image/jpeg", new byte[0]);

        // Act & Assert
        assertThrows(MushroomImageMissingException.class, () -> classificationService.classifyMushroom(user, file, LocalDateTime.now()));
    }

    @Test
    @Tag("Unit_Testing")
    void test_classifyMushroom_invalidContentType() {
        // Arrange
        MockMultipartFile nullContentTypeFile = new MockMultipartFile("file", "test_image.jpg", null, validImageContent);
        MockMultipartFile invalidContentTypeFile = new MockMultipartFile("file", "test_image.jpg", "random_content_type", validImageContent);

        // Act & Assert
        assertThrows(ImageTypeNotSupportedException.class, () -> classificationService.classifyMushroom(user, nullContentTypeFile, LocalDateTime.now()));
        assertThrows(ImageTypeNotSupportedException.class, () -> classificationService.classifyMushroom(user, invalidContentTypeFile, LocalDateTime.now()));
    }

    @Test
    @Tag("Unit_Testing")
    void test_classifyMushroom_errorSavingImage() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test_image.jpg", "image/jpeg", validImageContent);

        Mockito.when(imageRepository.saveImageToStorage(user.getId().toString(), file)).thenThrow(IOException.class);

        // Act & Assert
        assertThrows(MushroomImageProcessingException.class, () -> classificationService.classifyMushroom(user, file, LocalDateTime.now()));
        Mockito.verify(imageRepository, Mockito.times(1)).saveImageToStorage(user.getId().toString(), file);
    }

    @Test
    @Tag("Unit_Testing")
    void getImage_imageReadSuccessful() throws IOException {
        // Arrange
        String imageName = "test_image.jpg";

        Mockito.when(imageRepository.getImage(user.getId().toString(), imageName)).thenReturn(validImageContent);

        // Act
        byte[] retrievedImage = assertDoesNotThrow(() -> classificationService.getImage(user.getId(), imageName));

        // Assert
        assertNotNull(retrievedImage);
        assertArrayEquals(validImageContent, retrievedImage);
        Mockito.verify(imageRepository, Mockito.times(1)).getImage(user.getId().toString(), imageName);
    }

    @Test
    @Tag("Unit_Testing")
    void getImage_imageReadFailed() throws IOException {
        // Arrange
        String imageName = "test_image.jpg";

        Mockito.when(imageRepository.getImage(user.getId().toString(), imageName)).thenThrow(IOException.class);

        // Act & Assert
        assertThrows(MushroomImageRetrievalException.class, () -> classificationService.getImage(user.getId(), imageName));
        Mockito.verify(imageRepository, Mockito.times(1)).getImage(user.getId().toString(), imageName);
    }

    @Test
    @Tag("Unit_Testing")
    void test_getAllMushroomInstancesForUser() {
        // Arrange
        List<MushroomInstance> expectedMushroomInstances = List.of(
                new MushroomInstance(user, "classification1", "image1.jpg", LocalDateTime.now()),
                new MushroomInstance(user, "classification2", "image2.jpg", LocalDateTime.now())
        );

        Mockito.when(classificationRepository.findAllByUser(user)).thenReturn(expectedMushroomInstances);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");

        // Act
        List<MushroomClassificationDTO> mushroomClassificationDTOS = classificationService.getAllMushroomInstancesForUser(user);

        // Assert
        assertNotNull(mushroomClassificationDTOS);
        assertEquals(expectedMushroomInstances.size(), mushroomClassificationDTOS.size());

        assertEquals(mushroomClassificationDTOS.get(0).classificationResult, expectedMushroomInstances.get(0).getClassificationResult());
        assertEquals(mushroomClassificationDTOS.get(0).sampleTakenAt, formatter.format(expectedMushroomInstances.get(0).getSampleTakenAt()));
        assertEquals(mushroomClassificationDTOS.get(0).mushroomInstanceId, expectedMushroomInstances.get(0).getId());

        assertEquals(mushroomClassificationDTOS.get(1).classificationResult, expectedMushroomInstances.get(1).getClassificationResult());
        assertEquals(mushroomClassificationDTOS.get(1).sampleTakenAt, formatter.format(expectedMushroomInstances.get(1).getSampleTakenAt()));
        assertEquals(mushroomClassificationDTOS.get(1).mushroomInstanceId, expectedMushroomInstances.get(1).getId());

        Mockito.verify(classificationRepository, Mockito.times(1)).findAllByUser(user);
    }

    @Test
    @Tag("Unit_Testing")
    void getMushroomInstanceForUser_getSuccessful() {
        // Arrange
        MushroomInstance expectedMushroomInstance = new MushroomInstance(user, "classification1", "image1.jpg", LocalDateTime.now());
        expectedMushroomInstance.setId(1L);

        Mockito.when(classificationRepository.findById(expectedMushroomInstance.getId())).thenReturn(java.util.Optional.of(expectedMushroomInstance));

        // Act
        MushroomInstance foundMushroomInstance = classificationService.getMushroomInstanceForUser(expectedMushroomInstance.getId(), user.getId());

        // Assert
        assertNotNull(foundMushroomInstance);
        assertEquals(expectedMushroomInstance, foundMushroomInstance);
        Mockito.verify(classificationRepository, Mockito.times(1)).findById(expectedMushroomInstance.getId());
    }

    @Test
    @Tag("Unit_Testing")
    void getMushroomInstanceForUser_noInstanceFound() {
        // Arrange
        Long expectedMushroomInstanceId = 1L;

        Mockito.when(classificationRepository.findById(expectedMushroomInstanceId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(MushroomNotFoundException.class, () -> classificationService.getMushroomInstanceForUser(expectedMushroomInstanceId, user.getId()));
        Mockito.verify(classificationRepository, Mockito.times(1)).findById(expectedMushroomInstanceId);
    }

    @Test
    @Tag("Unit_Testing")
    void getMushroomInstanceForUser_userMismatch() {
        // Arrange
        MushroomInstance expectedMushroomInstance = new MushroomInstance(user, "classification1", "image1.jpg", LocalDateTime.now());
        expectedMushroomInstance.setId(1L);

        Mockito.when(classificationRepository.findById(expectedMushroomInstance.getId())).thenReturn(java.util.Optional.of(expectedMushroomInstance));

        // Act & Assert
        assertThrows(MushroomNotFoundException.class, () -> classificationService.getMushroomInstanceForUser(expectedMushroomInstance.getId(), 2L));
        Mockito.verify(classificationRepository, Mockito.times(1)).findById(expectedMushroomInstance.getId());
    }

    @Test
    @Tag("Unit_Testing")
    void mapToDTO() {
        // Arrange
        MushroomInstance mushroomInstance = new MushroomInstance(user, "classification1", "image1.jpg", LocalDateTime.now());
        mushroomInstance.setId(1L);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");
        String dateString = formatter.format(mushroomInstance.getSampleTakenAt());

        // Act
        MushroomClassificationDTO mushroomClassificationDTO = classificationService.mapToDTO(mushroomInstance);

        // Assert
        assertNotNull(mushroomClassificationDTO);
        assertEquals(mushroomInstance.getId(), mushroomClassificationDTO.mushroomInstanceId);
        assertEquals(mushroomInstance.getClassificationResult(), mushroomClassificationDTO.classificationResult);
        assertEquals(dateString, mushroomClassificationDTO.sampleTakenAt);
    }
}