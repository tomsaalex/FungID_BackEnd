package com.example.fungid.dto;

import jakarta.validation.constraints.Size;

public class MushroomClassificationDTO {
    @Size(max = 50)
    private String classificationResult;

    public MushroomClassificationDTO(String classificationResult) {
        this.classificationResult = classificationResult;
    }

    public MushroomClassificationDTO() {
    }

    public String getClassificationResult() {
        return classificationResult;
    }

    public void setClassificationResult(String classificationResult) {
        this.classificationResult = classificationResult;
    }
}
