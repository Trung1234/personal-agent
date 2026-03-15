package com.example.personal_agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    private Integer id;
    private Integer taskId;
    private Integer subtaskId; // Có thể null
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String googleEventId;
    private String location;
}
