package com.example.fungid.repository;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Repository
public class ImageRepository {

    private final Path basePath;

    public ImageRepository(Path basePath) {
        this.basePath = basePath;
    }

    public Path saveImageToStorage(String uploadDirectory, MultipartFile imageFile) throws IOException {
        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

        Path uploadPath = basePath.resolve(uploadDirectory);
        Path filePath = uploadPath.resolve(uniqueFileName);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }

    public byte[] getImage(String imageDirectory, String imageName) throws IOException {
        Path imagePath = basePath.resolve(imageDirectory).resolve(imageName);

        if (Files.exists(imagePath)) {
            return Files.readAllBytes(imagePath);
        } else {
            return new byte[0];
        }
    }

    public FileSystemResource getImageAsResource(Path imagePath) {
        if (Files.exists(imagePath)) {
            return new FileSystemResource(imagePath);
        }
        else {
            return null;
        }
    }
}
