package com.example.fungid.dto;

import jakarta.validation.constraints.Size;

public class MushroomClassificationDTO {
    public Long mushroomInstanceId;
    public String sampleTakenAt;
    @Size(max = 50)
    public String classificationResult;

    public MushroomClassificationDTO(Long mushroomInstanceId, String classificationResult, String sampleTakenAt) {
        this.mushroomInstanceId = mushroomInstanceId;
        this.classificationResult = classificationResult;
        this.sampleTakenAt = sampleTakenAt;
    }
}
