package com.example.fungid.dto;

import jakarta.validation.constraints.Size;

public class MushroomClassificationDTO {

    public Long mushroomInstanceId;
    @Size(max = 50)
    public String classificationResult;

    public MushroomClassificationDTO(Long mushroomInstanceId, String classificationResult) {
        this.mushroomInstanceId = mushroomInstanceId;
        this.classificationResult = classificationResult;
    }
}
