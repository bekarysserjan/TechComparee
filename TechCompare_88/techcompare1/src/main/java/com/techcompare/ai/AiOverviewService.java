package com.techcompare.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class AiOverviewService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public AiOverviewService(
            RestClient.Builder restClientBuilder,
            @Value("${app.gemini.api-key:}") String apiKey,
            @Value("${app.gemini.model:gemini-3.8-flash}") String model
    ) {
        this.restClient = restClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String createOverview(AiOverviewRequest request) {
        validateRequest(request);

        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Gemini is not configured. Set the GEMINI_API_KEY environment variable."
            );
        }

        String prompt = buildPrompt(request);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", 700
                )
        );

        try {
            JsonNode response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode textNode = response == null
                    ? null
                    : response.path("candidates").path(0)
                              .path("content").path("parts").path(0).path("text");

            if (textNode == null || textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Gemini returned an empty overview."
                );
            }

            return textNode.asText().trim();
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            // Do not return API keys, request headers, or raw provider errors to the client.
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not generate the AI overview. Check the Gemini model and API configuration."
            );
        }
    }

    private void validateRequest(AiOverviewRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }
        if (request.deviceType() == null || request.deviceType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deviceType is required.");
        }
        if (!request.deviceType().equalsIgnoreCase("laptop")
                && !request.deviceType().equalsIgnoreCase("smartphone")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "deviceType must be 'laptop' or 'smartphone'."
            );
        }
        if (request.results() == null || request.results().size() < 2 || request.results().size() > 10) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "results must contain between 2 and 10 devices."
            );
        }
        if (request.categoryWeights() == null || request.categoryWeights().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryWeights are required.");
        }

        int totalWeight = 0;
        for (Map.Entry<String, Integer> entry : request.categoryWeights().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null
                    || entry.getValue() < 0 || entry.getValue() > 100) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category names must be present and weights must be integers from 0 to 100."
                );
            }
            totalWeight += entry.getValue();
        }
        if (totalWeight != 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category weights must add up to exactly 100."
            );
        }
        if (request.results().stream().anyMatch(result -> result == null
                || result.deviceId() == null || result.deviceName() == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Each result must contain a device ID and device name."
            );
        }
        long uniqueIds = request.results().stream().map(AiDeviceResult::deviceId).distinct().count();
        if (uniqueIds != request.results().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device IDs in results must be unique.");
        }
    }

    private String buildPrompt(AiOverviewRequest request) {
        String data = """
                Device type: %s
                User-selected category weights: %s
                Calculated comparison results: %s
                """.formatted(request.deviceType(), request.categoryWeights(), request.results());

        return """
                You are TechCompare's technical comparison explainer.
                Explain the supplied comparison data in clear, concise language for a general audience.
                Treat all device scores, ranks, category scores, weights, and availability values as fixed facts
                calculated by the application. Never recalculate, modify, infer, or contradict them.
                Do not choose a different winner. If a result has a null overall score or rank, explain that
                there is insufficient score coverage rather than treating it as zero.
                Explain the top-ranked device under the selected weights, the most important category
                differences, and how the user's selected weights influence the ranking. Mention that these
                are model-based scores, not a guarantee of real-world performance. Do not invent hardware
                specifications, benchmark values, prices, or facts not present in the supplied data.
                Use short paragraphs and, where helpful, a few bullet points. Aim for 120-200 words.
                Return only the explanation text, without JSON or markdown code fences.

                DATA:
                %s
                """.formatted(data);
    }
}
