package com.example.personal_agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String email;
    private String name;
    private String googleId; // Lưu ID duy nhất từ Google để tránh trùng lặp
    private String avatarUrl;
    private LocalDateTime createdAt;
}