package com.example.personal_agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Kết quả tổng thể mà AI phải trả về

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public class PlanResponse {

    @JsonProperty("todos")
    private List<TodoItem> todos;

    @JsonProperty("schedules")
    private List<ScheduleItem> schedules;

    // Constructor rỗng (Bắt buộc để Jackson parse JSON)
    public PlanResponse() {}

    // Constructor đầy đủ
    public PlanResponse(List<TodoItem> todos, List<ScheduleItem> schedules) {
        this.todos = todos;
        this.schedules = schedules;
    }

    // Getters và Setters
    public List<TodoItem> getTodos() { return todos; }
    public void setTodos(List<TodoItem> todos) { this.todos = todos; }

    public List<ScheduleItem> getSchedules() { return schedules; }
    public void setSchedules(List<ScheduleItem> schedules) { this.schedules = schedules; }

    // --- Inner Class: TodoItem ---
    public static class TodoItem {
        @JsonProperty("title")
        private String title;

        @JsonProperty("estimated_minutes")
        private Integer estimatedMinutes;

        public TodoItem() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Integer getEstimatedMinutes() { return estimatedMinutes; }
        public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    }

    // --- Inner Class: ScheduleItem ---
    public static class ScheduleItem {
        @JsonProperty("title")
        private String title;

        @JsonProperty("start")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime startTime;

        @JsonProperty("end")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime endTime;

        public ScheduleItem() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
}