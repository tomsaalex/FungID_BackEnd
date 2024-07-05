package com.example.fungid.service;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import com.example.fungid.dto.ClassificationResultAI;
import com.example.fungid.dto.MushroomClassificationDTO;
import com.example.fungid.exceptions.mushroom_id.*;
import com.example.fungid.repository.ClassificationRepository;
import com.example.fungid.repository.ImageRepository;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ClassificationService {
    private final ClassificationRepository classificationRepository;
    private final ImageRepository imageRepository;

    private final String IMAGE_UPLOAD_DIRECTORY = "src/main/resources/static/images/mushroom_instances";

    public ClassificationService(ClassificationRepository classificationRepository, ImageRepository imageRepository) {
        this.classificationRepository = classificationRepository;
        this.imageRepository = imageRepository;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg");
    }

    public MushroomClassificationDTO classifyMushroom(User user, MultipartFile mushroomImage, LocalDateTime mushroomDate) {
        String userImageDirectory = IMAGE_UPLOAD_DIRECTORY + "/" + user.getId();

        if (mushroomImage.isEmpty())
            throw new MushroomImageMissingException("You must provide an image of the mushroom.");

        if (mushroomImage.getContentType() == null || !isSupportedContentType(mushroomImage.getContentType()))
            throw new ImageTypeNotSupportedException("Only PNG and JPG images are supported.");

        try {
            String imageName = imageRepository.saveImageToStorage(userImageDirectory, mushroomImage);
            Path mushroomImageFilePath = Path.of(userImageDirectory, imageName);
            String classificationResult = classifyMushroomWithAIModel(mushroomImageFilePath);

            MushroomInstance classifiedMushroom = new MushroomInstance(user, classificationResult, imageName, mushroomDate);

            classifiedMushroom = classificationRepository.save(classifiedMushroom);

            return mapToDTO(classifiedMushroom);
        } catch (IOException e) {
            throw new MushroomImageProcessingException("Error processing mushroom image");
        }
    }

    public byte[] getImage(Long userId, String mushroomImageName) {
        try {
            return imageRepository.getImage(IMAGE_UPLOAD_DIRECTORY + "/" + userId, mushroomImageName);
        } catch (IOException e) {
            throw new MushroomImageRetrievalException("Error retrieving mushroom image");
        }
    }

    public List<MushroomClassificationDTO> getAllMushroomInstancesForUser(User user) {
        List<MushroomInstance> mushroomInstances = classificationRepository.findAllByUser(user);
        return mushroomInstances.stream().map(this::mapToDTO).toList();
    }

    public String classifyMushroomWithAIModel(Path mushroomImageFilePath) {
        FileSystemResource imageResource = imageRepository.getImageAsResource(mushroomImageFilePath);

        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String AI_MODEL_URL = "http://localhost:5000";
        String requestAddress = AI_MODEL_URL + "/api/classifications/identify";

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<ClassificationResultAI> response = restTemplate.postForEntity(requestAddress, requestEntity, ClassificationResultAI.class);
        ClassificationResultAI mushroomClassificationResult = response.getBody();

        return mushroomClassificationResult != null ? mushroomClassificationResult.classificationResult() : null;
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
