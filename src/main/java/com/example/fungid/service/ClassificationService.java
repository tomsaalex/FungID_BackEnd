package com.example.fungid.service;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import com.example.fungid.dto.ClassificationResultAI;
import com.example.fungid.dto.MushroomClassificationDTO;
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

    public MushroomClassificationDTO classifyMushroom(User user, MultipartFile mushroomImage) throws IOException {
        String userImageDirectory = IMAGE_UPLOAD_DIRECTORY + "/" + user.getId();

        String imageName = imageRepository.saveImageToStorage(userImageDirectory, mushroomImage);

        Path mushroomImageFilePath = Path.of(userImageDirectory, imageName);
        String classificationResult = classifyMushroomWithAIModel(mushroomImageFilePath);

        MushroomInstance classifiedMushroom = new MushroomInstance(user, classificationResult, imageName);

        classifiedMushroom = classificationRepository.save(classifiedMushroom);

        return mapToDTO(classifiedMushroom);
    }

    public byte[] getImage(Long userId, String mushroomImageName) throws IOException {
        return imageRepository.getImage(IMAGE_UPLOAD_DIRECTORY + "/" + userId, mushroomImageName);
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

    public MushroomInstance getMushroomInstance(Long id) {
        return classificationRepository.findById(id).orElse(null);
    }

    public MushroomClassificationDTO mapToDTO(MushroomInstance mushroomInstance) {
        return new MushroomClassificationDTO(mushroomInstance.getId(), mushroomInstance.getClassificationResult());
    }
}
