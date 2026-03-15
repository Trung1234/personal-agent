package com.example.personal_agent.controller;


import com.example.personal_agent.dto.PlanResponse;
import com.example.personal_agent.model.Schedule;
import com.example.personal_agent.model.SubTask;
import com.example.personal_agent.model.Task;
import com.example.personal_agent.repository.ScheduleMapper;
import com.example.personal_agent.repository.SubTaskMapper;
import com.example.personal_agent.repository.TaskMapper;
import com.example.personal_agent.service.GoogleCalendarService;
import com.example.personal_agent.service.TaskAIService;
import com.google.api.client.util.DateTime; // Class DateTime của Google
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes("plan") // Giữ object PlanResponse trong session để dùng ở bước tiếp theo
public class TaskController {

    private final TaskAIService taskAIService;
    private final GoogleCalendarService googleCalendarService;
    private final TaskMapper taskMapper;
    private final SubTaskMapper subTaskMapper;
    private final ScheduleMapper scheduleMapper;

    // Dịch vụ này giúp lấy Access Token từ Security Context
    private final OAuth2AuthorizedClientService authorizedClientService;

    // --- Helper: Lấy Google Access Token từ Security Context ---
    private String getGoogleAccessToken(OAuth2AuthenticationToken authentication) {
        if (authentication == null) return null;

        // "google" là registrationId trong application.properties
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                "google",
                authentication.getName()
        );

        // Kiểm tra token còn hạn không (Spring Boot OAuth2 Client tự động refresh nếu cấu hình đúng)
        return client != null ? client.getAccessToken().getTokenValue() : null;
    }

    // --- Helper: Lấy Email của User đang login ---
    private String getCurrentUserEmail(OAuth2AuthenticationToken authentication) {
        return (String) authentication.getPrincipal().getAttributes().get("email");
    }

    // 1. Hiển thị Form nhập liệu (Node 1)
    @GetMapping("/tasks/new")
    public String showCreateForm() {
        return "create-task";
    }

    // 2. Xử lý Logic: Lấy lịch -> Gọi AI -> Show Preview (Node 2, 3, 4)
    @PostMapping("/tasks/generate")
    public String generatePlan(
            @RequestParam String description,
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication,
            Model model) {

        // 1. Lấy Token và Email
        String accessToken = getGoogleAccessToken(authentication);
        if (accessToken == null) {
            return "redirect:/login"; // Nếu chưa login hoặc hết token, quay về login
        }
        String userEmail = getCurrentUserEmail(authentication);

        // 2. Lấy lịch bận từ Google Calendar (Node 2)
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime end = now.plusDays(7); // Lấy lịch trong 7 ngày tới

        // Lấy danh sách bận
        List<com.google.api.services.calendar.model.TimePeriod> busySlots =
                googleCalendarService.getBusySlots(accessToken, now, end);

        // 3. Chuyển đổi lịch bận thành chuỗi ngữ cảnh cho AI
        String busyContext = convertBusySlotsToString(busySlots);

        // 4. Gọi AI sinh kế hoạch (Node 3)
        PlanResponse plan = taskAIService.generatePlan(description, busyContext);

        // 5. Lưu plan vào Session để dùng ở bước Confirm sau này
        model.addAttribute("plan", plan);
        model.addAttribute("originalDescription", description);

        // 6. Trả về View Preview (Node 4)
        return "task-preview";
    }

    // 3. Xử lý Lưu Database & Đồng bộ Google Calendar (Node 5)
    @PostMapping("/tasks/confirm")
    public String confirmPlan(
            @ModelAttribute("plan") PlanResponse plan, // Lấy object từ Session
            @RequestParam("originalDescription") String description,
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String accessToken = getGoogleAccessToken(authentication);
            String userEmail = getCurrentUserEmail(authentication);

            // A. Lưu Task cha vào SQL Server
            Task newTask = new Task();
            newTask.setUserId(1); // TODO: Lấy real user ID từ DB bằng email
            newTask.setDescription(description);
            newTask.setStatus("IN_PROGRESS");
            newTask.setCreatedAt(LocalDateTime.now());

            taskMapper.insert(newTask); // MyBatis insert, newTask.id sẽ được tự động gán (nếu cấu hình <selectKey>)

            int taskId = newTask.getId(); // Lấy ID vừa tạo

            // B. Lưu SubTasks (Todo list)
            if (plan.getTodos() != null) {
                for (PlanResponse.TodoItem todo : plan.getTodos()) {
                    SubTask sub = new SubTask();
                    sub.setTaskId(taskId);
                    sub.setTitle(todo.getTitle());
                    sub.setIsCompleted(false);
                    subTaskMapper.insert(sub);
                }
            }

            // C. Lưu Schedules & Đồng bộ Google Calendar
            if (plan.getSchedules() != null && accessToken != null) {
                for (PlanResponse.ScheduleItem item : plan.getSchedules()) {

                    // 1. Tạo sự kiện trên Google Calendar trước để lấy ID
                    String googleEventId = googleCalendarService.createEvent(
                            accessToken,
                            item.getTitle(),
                            item.getStartTime().atZone(ZoneId.systemDefault()), // Chuyển LocalDateTime sang ZonedDateTime
                            item.getEndTime().atZone(ZoneId.systemDefault())
                    );

                    // 2. Lưu vào Database SQL Server
                    Schedule schedule = new Schedule();
                    schedule.setTaskId(taskId);
                    schedule.setTitle(item.getTitle());
                    schedule.setStartTime(item.getStartTime());
                    schedule.setEndTime(item.getEndTime());
                    schedule.setGoogleEventId(googleEventId); // Lưu ID để quản lý sau này

                    scheduleMapper.insert(schedule);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đã lên kế hoạch và đồng bộ lịch thành công!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi lưu: " + e.getMessage());
            return "redirect:/tasks/new";
        }
    }

    // Helper nhỏ để chuyển list busy slots thành chuỗi cho AI dễ hiểu
    private String convertBusySlotsToString(List<com.google.api.services.calendar.model.TimePeriod> busySlots) {
        if (busySlots == null || busySlots.isEmpty()) {
            return "Bạn hoàn toàn rảnh trong thời gian này.";
        }

        StringBuilder sb = new StringBuilder("Bạn bận vào các khung giờ sau: ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");

        for (com.google.api.services.calendar.model.TimePeriod period : busySlots) {
            // Convert Google DateTime -> Java Instant -> LocalDateTime
            Instant startInstant = Instant.parse(period.getStart().toString());
            LocalDateTime start = LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault());

            sb.append(start.format(formatter)).append(", ");
        }
        return sb.toString();
    }
}