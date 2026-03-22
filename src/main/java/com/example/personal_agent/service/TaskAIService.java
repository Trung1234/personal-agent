package com.example.personal_agent.service;


import com.example.personal_agent.dto.PlanResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TaskAIService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public TaskAIService(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public PlanResponse generatePlan(String userTask, String freeSlotsText) {

        String prompt = """
    Bạn là trợ lý quản lý công việc chuyên nghiệp.
    
    Yêu cầu của người dùng: %s
    Lịch trống: %s
    
    QUAN TRỌNG: 
    - CHỈ trả về JSON thuần túy, không có markdown, không có giải thích, không có ```json.
    - Phải đúng cấu trúc sau (không được thay đổi tên field):
    
    {
      "todos": [
        {"title": "Tiêu đề việc cần làm 1", "estimated_minutes": 30},
        {"title": "Tiêu đề việc cần làm 2", "estimated_minutes": 45}
      ],
      "schedules": [
        {
          "title": "Tiêu đề lịch",
          "start": "2025-03-23T09:00",
          "end": "2025-03-23T10:00"
        }
      ]
    }
    
    start và end phải là định dạng ISO: yyyy-MM-dd'T'HH:mm (không có giây).
    """.formatted(userTask, freeSlotsText);

        // 1. Gọi AI
        String jsonResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        // 2. Clean markdown và text thừa
        String cleanJson = jsonResponse;
        int startIndex = cleanJson.indexOf("{");
        int endIndex = cleanJson.lastIndexOf("}");

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            cleanJson = cleanJson.substring(startIndex, endIndex + 1);
        } else {
            throw new IllegalArgumentException("Không tìm thấy JSON hợp lệ trong phản hồi của AI");
        }
        try {
            // 3. Parse
            PlanResponse planResponse = objectMapper.readValue(cleanJson, PlanResponse.class);
            return planResponse;
        } catch (JsonProcessingException e) {
            System.err.println("=== JSON bị lỗi ===");
            System.err.println("Raw response: " + jsonResponse);
            e.printStackTrace();
            return new PlanResponse();
        }
    }
}