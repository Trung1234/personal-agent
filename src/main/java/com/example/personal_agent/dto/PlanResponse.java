package com.example.personal_agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Kết quả tổng thể mà AI phải trả về
public record PlanResponse(
        @JsonProperty("todos") List<TodoItem> todos,
        @JsonProperty("schedules") List<ScheduleItem> schedules
) {}