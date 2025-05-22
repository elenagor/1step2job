package com.ostj.resumeprocessing;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ostj.entity.ChatGPTRequest;
import com.ostj.entity.ChatGPTResponse;

public class OpenAIClient {

    String apiKey;
    String endpoint;
    String model;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAIClient(String endpoint, String apiKey, String model) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String getChatGPTResponse(String userMessage) throws Exception {
        ChatGPTRequest.Message userMsg = new ChatGPTRequest.Message();
        userMsg.setRole("user");
        userMsg.setContent(userMessage);

        ChatGPTRequest request = new ChatGPTRequest();
        request.setModel(model);
        request.setMessages(List.of(userMsg));

        String requestBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI(endpoint))
                .header("Content-Type", "application/json")
                //.header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ChatGPTResponse chatGPTResponse = objectMapper.readValue(response.body(), ChatGPTResponse.class);
            if (chatGPTResponse.getChoices() != null && !chatGPTResponse.getChoices().isEmpty()) {
                return chatGPTResponse.getChoices().get(0).getMessage().getContent();
            } else {
                return "No response from ChatGPT";
            }
        } else {
            throw new RuntimeException("Error: " + response.body());
        }
    }
}
