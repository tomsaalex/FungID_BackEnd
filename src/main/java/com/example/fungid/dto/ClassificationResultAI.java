package com.example.fungid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClassificationResultAI(String classificationResult) { }
