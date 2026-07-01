package com.smartspend.backend.service;

import com.smartspend.backend.dto.MlServiceDtos;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Talks to the standalone Python/FastAPI ML microservice that does
 * text classification (TF-IDF + Logistic Regression) on expense
 * descriptions. Kept as a thin client: this class knows nothing about
 * the model itself, only the REST contract - so swapping the ML
 * service's internals (or even its language) later wouldn't require
 * any change here.
 */
@Service
public class MlClassifierClient {

    private static final Logger log = Logger.getLogger(MlClassifierClient.class.getName());

    private final WebClient mlServiceWebClient;

    public MlClassifierClient(WebClient mlServiceWebClient) {
        this.mlServiceWebClient = mlServiceWebClient;
    }

    /**
     * Calls the ML microservice to predict a category for the given
     * expense description. Falls back to a safe default ("Other",
     * confidence 0.0) if the service is unreachable or errors out -
     * categorization is a nice-to-have, not something that should ever
     * block a user from saving an expense.
     */
    public MlServiceDtos.CategorizeResponse categorize(String description) {
        try {
            return mlServiceWebClient.post()
                    .uri("/categorize")
                    .bodyValue(new MlServiceDtos.CategorizeRequest(description))
                    .retrieve()
                    .bodyToMono(MlServiceDtos.CategorizeResponse.class)
                    .timeout(Duration.ofSeconds(35))
                    .onErrorResume(e -> {
                        log.log(Level.WARNING, "ML service call failed, falling back to 'Other'", e);
                        return Mono.just(new MlServiceDtos.CategorizeResponse("Other", 0.0));
                    })
                    .block();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unexpected error calling ML service", e);
            return new MlServiceDtos.CategorizeResponse("Other", 0.0);
        }
    }
}
