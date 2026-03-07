package com.example.personal_agent.service;


import com.example.personal_agent.dto.PlanResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TaskAIService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper; // Dùng để parse JSON String sang Object


    public TaskAIService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public PlanResponse generatePlan(String userTask, String freeSlotsText) {

        String prompt = """
            Bạn là trợ lý quản lý công việc.
            Yêu cầu: %s
            Lịch trống: %s
            s
            QUAN TRỌNG: Chỉ trả về JSON thuần túy, không có markdown (ví dụ không dùng ```json), không có giải thích văn bản.
            Cấu trúc: { "todos": [ ... ], "schedules": [ ... ] }
            """.formatted(userTask, freeSlotsText);

        // 1. Gọi AI nhận về String JSON
        String jsonResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content(); // Trả về String

        try {
            // 2. Clean string (Xử lý trường hợp AI lỡ thêm markdown ```json ... ```)
            String cleanJson = jsonResponse;
            if (jsonResponse.contains("```json")) {
                cleanJson = jsonResponse.replace("```json", "").replace("```", "").trim();
            } else if (jsonResponse.contains("```")) {
                cleanJson = jsonResponse.replace("```", "").trim();
            }

            // 3. Parse String thành Object PlanResponse
            return objectMapper.readValue(cleanJson, PlanResponse.class);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Trả về object rỗng hoặc throw exception tùy logic xử lý lỗi của bạn
            return new PlanResponse();
        }
    }
}