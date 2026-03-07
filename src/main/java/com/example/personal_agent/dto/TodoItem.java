package com.example.personal_agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Lưu trữ danh sách việc cần làm (không có giờ cụ thể hoặc gom gọn)
public record TodoItem(
        @JsonProperty("title") String title,
        @JsonProperty("estimated_minutes") Integer estimatedMinutes
) {}

