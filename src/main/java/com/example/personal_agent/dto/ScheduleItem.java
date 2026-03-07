package com.example.personal_agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

// Lưu trữ chi tiết lịch trình có giờ giấc
public record ScheduleItem(
        @JsonProperty("title") String title,
        @JsonProperty("start") LocalDateTime startTime, // Format: yyyy-MM-ddTHH:mm
        @JsonProperty("end") LocalDateTime endTime
) {}

