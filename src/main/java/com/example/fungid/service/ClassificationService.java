package com.example.fungid.service;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import com.example.fungid.dto.MushroomClassificationDTO;
import com.example.fungid.exceptions.mushroom_id.*;
import com.example.fungid.repository.ClassificationRepository;
import com.example.fungid.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ClassificationService {
    private final NetworkService networkService;
    private final ClassificationRepository classificationRepository;
    private final ImageRepository imageRepository;

    @Autowired
    public ClassificationService(NetworkService networkService, ClassificationRepository classificationRepository, ImageRepository imageRepository) {
        this.networkService = networkService;
        this.classificationRepository = classificationRepository;
        this.imageRepository = imageRepository;
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/jpg")
                || contentType.equals("image/jpeg")
                || contentType.equals("image/png");
    }

    public MushroomClassificationDTO classifyMushroom(User user, MultipartFile mushroomImage, LocalDateTime mushroomDate) {
        if (mushroomImage.isEmpty())
            throw new MushroomImageMissingException("You must provide an image of the mushroom.");

        if (mushroomImage.getContentType() == null || !isSupportedContentType(mushroomImage.getContentType()))
            throw new ImageTypeNotSupportedException("Only PNG and JPG images are supported.");

        try {
            Path imageFilePath = imageRepository.saveImageToStorage(user.getId().toString(), mushroomImage);
            String imageName = imageFilePath.getFileName().toString();

            FileSystemResource imageResource = imageRepository.getImageAsResource(imageFilePath);
            String classificationResult = networkService.classifyMushroomWithAIModel(imageResource);

            MushroomInstance classifiedMushroom = new MushroomInstance(user, classificationResult, imageName, mushroomDate);

            classifiedMushroom = classificationRepository.save(classifiedMushroom);

            return mapToDTO(classifiedMushroom);
        } catch (IOException e) {
            throw new MushroomImageProcessingException("Error processing mushroom image.");
        }
    }

    public byte[] getImage(Long userId, String mushroomImageName) {
        try {
            return imageRepository.getImage(userId.toString(), mushroomImageName);
        } catch (IOException e) {
            throw new MushroomImageRetrievalException("Error retrieving mushroom image");
        }
    }

    public List<MushroomClassificationDTO> getAllMushroomInstancesForUser(User user) {
        List<MushroomInstance> mushroomInstances = classificationRepository.findAllByUser(user);
        return mushroomInstances.stream().map(this::mapToDTO).toList();
    }

    public MushroomInstance getMushroomInstanceForUser(Long mushroomInstanceId, Long userId) {
        MushroomInstance foundMushroom = classificationRepository.findById(mushroomInstanceId).orElse(null);

        if (foundMushroom == null || !foundMushroom.getUser().getId().equals(userId))
            throw new MushroomNotFoundException("Your account does not appear to have a mushroom classification job with the given ID.");

        return foundMushroom;
    }

    public MushroomClassificationDTO mapToDTO(MushroomInstance mushroomInstance) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");
        String dateString = formatter.format(mushroomInstance.getSampleTakenAt());
        return new MushroomClassificationDTO(mushroomInstance.getId(), mushroomInstance.getClassificationResult(), dateString);
    }
}
