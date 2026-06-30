package com.smartspend.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Google's Gemini API (generateContent endpoint).
 * Uses the free tier of Google AI Studio - no billing required, just
 * an API key from https://aistudio.google.com.
 */
@Service
public class AnthropicClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String model;

    private final WebClient anthropicWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicClient(WebClient anthropicWebClient) {
        this.anthropicWebClient = anthropicWebClient;
    }

    public String complete(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", userPrompt))
                        )
                ),
                "generationConfig", Map.of(
                        "maxOutputTokens", 8192
                )
        );

        String uri = "/v1beta/models/" + model + ":generateContent";

        String rawResponse = anthropicWebClient.post()
                .uri(uri)
                .header("x-goog-api-key", apiKey)
                .header("content-type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        return extractText(rawResponse);
    }

    private String extractText(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
            throw new IllegalStateException("Unexpected Gemini API response shape: " + rawResponse);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Gemini API response", e);
        }
    }
}