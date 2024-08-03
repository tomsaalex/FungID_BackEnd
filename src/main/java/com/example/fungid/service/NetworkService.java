package com.example.fungid.service;

import com.example.fungid.dto.ClassificationResultAI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class NetworkService {

    private final RestTemplate restTemplate;

    @Autowired
    public NetworkService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String classifyMushroomWithAIModel(FileSystemResource imageResource) {
        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", imageResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String AI_MODEL_URL = "http://localhost:5000";
        String requestAddress = AI_MODEL_URL + "/classifications/identify";

        ResponseEntity<ClassificationResultAI> response = restTemplate.postForEntity(requestAddress, requestEntity, ClassificationResultAI.class);
        ClassificationResultAI mushroomClassificationResult = response.getBody();

        return mushroomClassificationResult != null ? mushroomClassificationResult.classificationResult() : null;
    }
}
