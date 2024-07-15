package com.example.fungid.repository;

import com.example.fungid.config.TestFileSystemConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {TestFileSystemConfig.class})
@Import({TestFileSystemConfig.class, ImageRepository.class})
@DataJpaTest
//@ActiveProfiles("test")
class ImageRepositoryTest {
    @Autowired
    private ImageRepository imageRepository;

    private final Path basePath;
    private byte[] savedImageBytes = "test_image".getBytes();

    @Autowired
    ImageRepositoryTest(Path basePath) {
        this.basePath = basePath;
    }

    @BeforeEach
    void setUp() throws IOException {
        imageRepository = new ImageRepository(basePath);

        Files.createDirectories(basePath.resolve("10"));
        MockMultipartFile imageFile = new MockMultipartFile("test_image", "test_image.jpg", "image/jpeg", savedImageBytes);
        Files.copy(imageFile.getInputStream(), basePath.resolve("10").resolve("test_image.jpg"));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.delete(basePath.resolve("10").resolve("test_image.jpg"));
        Files.deleteIfExists(basePath.resolve("10"));
    }

    @Test
    @Tag("Integration_Testing")
    void test_saveImageToStorage_saveSuccessful() {
        String originalFileName = "mushroom.jpg";
        MockMultipartFile imageFile = new MockMultipartFile("mushroomImage", originalFileName, "image/jpeg", "mushroom".getBytes());

        Path filePath;
        String imageDir = basePath.resolve("1").toString();
        filePath = assertDoesNotThrow(() -> imageRepository.saveImageToStorage(imageDir, imageFile));
        String uniqueFileName = filePath.getFileName().toString();
        assertNotNull(uniqueFileName);
        assertTrue(uniqueFileName.contains(originalFileName));
    }

    @Test
    @Tag("Integration_Testing")
    void test_getImage_imageExists() {
        // Arrange
        String imageDir = "10";
        String imageName = "test_image.jpg";

        // Act
        byte[] imageBytes = assertDoesNotThrow(() -> imageRepository.getImage(imageDir, imageName));

        // Assert
        assertNotNull(imageBytes);
        assertTrue(imageBytes.length > 0);
        assertArrayEquals(savedImageBytes, imageBytes);
    }

    @Test
    @Tag("Integration_Testing")
    void test_getImage_imageDoesNotExist() {
        // Arrange
        String imageDir = "10";
        String imageName = "non_existent_image.jpg";

        // Act
        byte[] imageBytes = assertDoesNotThrow(() -> imageRepository.getImage(imageDir, imageName));

        // Assert
        assertNotNull(imageBytes);
        assertEquals(0, imageBytes.length);
    }

    @Test
    @Tag("Integration_Testing")
    void test_getImageAsResource_imageExists() throws IOException {
        // Arrange
        String imageDir = "10";
        String imageName = "test_image.jpg";

        // Act
        Path imagePath = basePath.resolve(imageDir).resolve(imageName);
        FileSystemResource imageResource = imageRepository.getImageAsResource(imagePath);

        // Assert
        assertNotNull(imageResource);
        assertTrue(imageResource.exists());
        assertArrayEquals(imageResource.getContentAsByteArray(), savedImageBytes);

    }

    @Test
    @Tag("Integration_Testing")
    void test_getImageAsResource_imageDoesNotExist() {
        // Arrange
        String imageDir = "10";
        String imageName = "non_existent_image.jpg";

        // Act
        Path imagePath = basePath.resolve(imageDir).resolve(imageName);
        FileSystemResource imageResource = imageRepository.getImageAsResource(imagePath);

        // Assert
        assertNull(imageResource);
    }
}