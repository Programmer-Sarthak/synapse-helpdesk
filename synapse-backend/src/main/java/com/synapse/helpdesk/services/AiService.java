package com.synapse.helpdesk.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class AiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String determinePriority(String title, String description) {
        String prompt = "Analyze this IT support ticket and return ONLY ONE WORD (CRITICAL, HIGH, MEDIUM, LOW) representing its urgency based on standard ITIL practices.\nTitle: " + title + "\nDescription: " + description;
        String response = callGroqApi(prompt);
        String upper = response.toUpperCase();
        if (upper.contains("CRITICAL")) return "CRITICAL";
        if (upper.contains("HIGH")) return "HIGH";
        if (upper.contains("LOW")) return "LOW";
        return "MEDIUM";
    }

    public String generateSmartReply(String title, String description, List<String> comments) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert IT Support Agent. Draft a professional, empathetic, and concise reply to the user to help resolve their issue or ask for necessary clarification. Do not include placeholder names or signatures, just the direct message body. Keep it under 4 sentences.\n\n");
        prompt.append("Ticket Title: ").append(title).append("\n");
        prompt.append("Ticket Description: ").append(description).append("\n");

        if (comments != null && !comments.isEmpty()) {
            prompt.append("Conversation History:\n");
            for (String comment : comments) {
                prompt.append("- ").append(comment).append("\n");
            }
        }
        prompt.append("\nWrite the next response:");

        return callGroqApi(prompt.toString());
    }

    private String callGroqApi(String promptText) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String escapedPrompt = promptText.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");

            String requestBody = "{"
                    + "\"model\": \"llama-3.1-8b-instant\","
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}]"
                    + "}";

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(apiUrl, request, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            return rootNode.path("choices").get(0).path("message").path("content").asText().trim();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("\n🛑 === GROQ API ERROR ===");
            System.err.println("Status: " + e.getStatusCode());
            System.err.println("Message: " + e.getResponseBodyAsString());
            System.err.println("==============================\n");
            return "AI is busy. Please try again in a few minutes.";
        } catch (Exception e) {
            e.printStackTrace();
            return "System error. Please try manually.";
        }
    }
}