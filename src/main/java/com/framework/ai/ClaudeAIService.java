package com.framework.ai;

// ╔══════════════════════════════════════════════════════════╗
// ║  CLAUDE AI SERVICE                                       ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Wraps Anthropic Claude API for use throughout framework ║
// ║  Used by: SelfHealingDriver, FailureAnalyzer,            ║
// ║           TestGenerator, PageObjectGenerator, Jarvis     ║
// ║                                                          ║
// ║  API key from CLAUDE_API_KEY environment variable        ║
// ║  Never hardcode API keys in source code                  ║
// ╚══════════════════════════════════════════════════════════╝

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ClaudeAIService {

    private static final Logger log =
            LogManager.getLogger(ClaudeAIService.class);

    private static final String API_URL =
            "https://api.anthropic.com/v1/messages";
    private static final String MODEL =
            "claude-sonnet-4-6";
    private static final String API_VERSION =
            "2023-06-01";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ClaudeAIService() {
        this.apiKey = System.getenv("CLAUDE_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException(
                    "CLAUDE_API_KEY environment variable not set. "
                            + "Run: export CLAUDE_API_KEY=your-key");
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        this.mapper = new ObjectMapper();

        log.info("ClaudeAIService initialised");
    }

    // Basic ask — used for simple Q&A style prompts
    public String ask(String prompt) {
        return ask(prompt, 1024);
    }

    public String ask(String prompt, int maxTokens) {
        try {
            Map<String, Object> body = Map.of(
                    "model", MODEL,
                    "max_tokens", maxTokens,
                    "messages", List.of(
                            Map.of("role", "user",
                                    "content", prompt)
                    )
            );

            String jsonBody = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", API_VERSION)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers
                            .ofString(jsonBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request,
                            HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Claude API error: " + response.statusCode()
                                + " - " + response.body());
            }

            JsonNode json = mapper.readTree(response.body());
            String text = json.get("content")
                    .get(0)
                    .get("text")
                    .asText();

            log.debug("Claude response received: {} chars",
                    text.length());
            return text;

        } catch (Exception e) {
            log.error("Claude API call failed: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Claude API call failed: "
                            + e.getMessage(), e);
        }
    }

    // Forces Claude to respond with ONLY JSON
    public JsonNode askForJson(String prompt) {
        try {
            String jsonPrompt = prompt
                    + "\n\nRespond with ONLY valid JSON. "
                    + "No markdown, no backticks, no explanation.";

            String rawResponse = ask(jsonPrompt, 2048);

            String cleaned = rawResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return mapper.readTree(cleaned);

        } catch (Exception e) {
            log.error("Failed to parse Claude JSON response: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Claude JSON parsing failed: "
                            + e.getMessage(), e);
        }
    }

    // For generating Java code — strips markdown fences
    public String askForCode(String prompt) {
        String response = ask(prompt, 4096);

        String cleaned = response
                .replaceAll("```java", "")
                .replaceAll("```", "")
                .trim();

        return cleaned;
    }
}