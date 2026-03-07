package com.example.personal_agent.controller;

import com.example.personal_agent.dto.PlanResponse;
import com.example.personal_agent.service.GoogleCalendarService;
import com.example.personal_agent.service.TaskAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.ZonedDateTime;
import java.util.List;

@Controller
public class TaskController {

    @Autowired
    private GoogleCalendarService googleCalendarService; // Code bước trước

    @Autowired
    private TaskAIService taskAIService; // Code vừa viết ở trên

    // Hiển thị form nhập liệu
    @GetMapping("/tasks/new")
    public String showCreateForm() {
        return "create-task"; // file Thymeleaf
    }

    // Xử lý logic: Lấy lịch -> Gọi AI -> Hiện kết quả
    @PostMapping("/tasks/generate")
    public ModelAndView generatePlan(@RequestParam String description) {
        // Giả lập Access Token (Trong thực tế lấy từ Security Context)
        String mockAccessToken = "ya29.a0Af...";

        // 1. NODE 2: Lấy lịch trống từ Google Calendar
        // Giả định tìm trong vòng 7 ngày tới
        ZonedDateTime start = ZonedDateTime.now();
        ZonedDateTime end = start.plusDays(7);

        var busySlots = googleCalendarService.getBusySlots(mockAccessToken, start, end);

        // Logic đơn giản hóa để tạo chuỗi giờ rảnh cho AI (Trong thực tế cần thuật toán trừ set)
        // Ở đây mình giả lập kết quả để bạn dễ hình dung:
        String freeSlotsText = "Hôm nay 19:00-21:00, Ngày mai 20:00-22:00";
        // (Bạn có thể viết code Java để convert `busySlots` thành chuỗi này)

        // 2. NODE 3: Gọi AI lên kế hoạch
        PlanResponse plan = taskAIService.generatePlan(description, freeSlotsText);

        // 3. Trả về view Preview để User duyệt (Node 4)
        ModelAndView mav = new ModelAndView("task-preview");
        mav.addObject("plan", plan);
        return mav;
    }
}
