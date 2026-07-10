package com.synapse.helpdesk.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class AiService {

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public AiService() {
        this.restClient = RestClient.create();
    }

    public String determinePriority(String title, String description) {

        String prompt = "You are an IT Support AI. Analyze this ticket and respond with " +
                "exactly ONE WORD representing its priority: LOW, MEDIUM, HIGH, or CRITICAL. \n" +
                "Title: " + title + "\n" +
                "Description: " + description;

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            String rawResponse = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .body(requestBody).retrieve()
                    .body(String.class);

            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(rawResponse);

            String priority = rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return priority.trim().toUpperCase();

        } catch (Exception e) {
            System.err.println("AI SERVICE ERROR: " + e.getMessage());
            e.printStackTrace();
            return "UNASSIGNED";
        }
    }
}