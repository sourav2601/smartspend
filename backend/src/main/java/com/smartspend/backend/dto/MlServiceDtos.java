package com.smartspend.backend.dto;

public class MlServiceDtos {

    /** Request sent to the Python ML microservice's /categorize endpoint. */
    public record CategorizeRequest(String description) {}

    /** Response received back: predicted category name + confidence 0-1. */
    public record CategorizeResponse(String category, Double confidence) {}
}
