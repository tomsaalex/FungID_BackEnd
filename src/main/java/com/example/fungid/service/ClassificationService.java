package com.example.fungid.service;

import com.example.fungid.domain.MushroomInstance;
import com.example.fungid.domain.User;
import com.example.fungid.dto.MushroomClassificationDTO;
import com.example.fungid.repository.ClassificationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ClassificationService {
    private final ClassificationRepository classificationRepository;
    private final WebClient webClient;

    public ClassificationService(ClassificationRepository classificationRepository, WebClient webClient) {
        this.classificationRepository = classificationRepository;
        this.webClient = webClient;
    }

    public MushroomClassificationDTO classifyMushroom(User user, String mushoomImageFileName) {
        String classificationResult = "Amanita muscaria"; // TODO: Replace with call to AI model
        MushroomInstance classifiedMushroom = new MushroomInstance(user, classificationResult, mushoomImageFileName);

        classifiedMushroom = classificationRepository.save(classifiedMushroom);

        return mapToDTO(classifiedMushroom);
    }

    public MushroomInstance getMushroomInstance(Long id) {
        return classificationRepository.findById(id).orElse(null);
    }

    public MushroomClassificationDTO mapToDTO(MushroomInstance mushroomInstance) {
        return new MushroomClassificationDTO(mushroomInstance.getClassificationResult());
    }
}
