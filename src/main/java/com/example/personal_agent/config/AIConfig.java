package com.example.personal_agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions; // Import đúng theo yêu cầu
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // Sử dụng GoogleGenAiChatOptions thay vì GeminiChatOptions
        return builder.defaultOptions(
                GoogleGenAiChatOptions.builder()
                        .temperature(0.7)
                        .model("gemini-2.5-flash")
                        .build()
        ).build();
    }
}