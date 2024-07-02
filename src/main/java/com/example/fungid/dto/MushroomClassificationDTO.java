package com.example.fungid.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class MushroomClassificationDTO {
    public Long mushroomInstanceId;
    public LocalDateTime sampleTakenAt;
    @Size(max = 50)
    public String classificationResult;

    public MushroomClassificationDTO(Long mushroomInstanceId, String classificationResult, LocalDateTime sampleTakenAt) {
        this.mushroomInstanceId = mushroomInstanceId;
        this.classificationResult = classificationResult;
        this.sampleTakenAt = sampleTakenAt;
    }
}
