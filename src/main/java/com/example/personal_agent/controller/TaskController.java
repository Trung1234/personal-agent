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
import com.google.api.client.util.DateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
@SessionAttributes("plan")
public class TaskController {

    private final TaskAIService taskAIService;
    private final GoogleCalendarService googleCalendarService;
    private final TaskMapper taskMapper;
    private final SubTaskMapper subTaskMapper;
    private final ScheduleMapper scheduleMapper;

    private final OAuth2AuthorizedClientService authorizedClientService;

    // ==================== HELPER MỚI ====================
    private String getGoogleAccessToken(Authentication authentication) {
        if (authentication == null || !(authentication instanceof OAuth2AuthenticationToken)) {
            return null;
        }
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                "google",
                token.getName()
        );
        return client != null ? client.getAccessToken().getTokenValue() : null;
    }

    private String getCurrentUserEmail(Authentication authentication) {
        if (authentication == null || !(authentication instanceof OAuth2AuthenticationToken)) {
            return null;
        }
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OidcUser user = (OidcUser) token.getPrincipal();   // ← theo yêu cầu của bạn
        return user.getEmail();
    }
    // ===================================================

    @GetMapping("/tasks/new")
    public String showCreateForm() {
        return "create-task";
    }

    @PostMapping("/tasks/generate")
    public String generatePlan(
            @RequestParam String description,
            Authentication authentication,   // ← KHÔNG dùng @AuthenticationPrincipal
            Model model) {

        System.out.println(">>> ĐÃ VÀO PHƯƠNG THỨC GENERATE PLAN. INPUT: " + description);

        // Kiểm tra authentication theo đúng cách bạn yêu cầu
        if (authentication == null || !(authentication instanceof OAuth2AuthenticationToken)) {
            return "redirect:/login";
        }

        String accessToken = getGoogleAccessToken(authentication);
        if (accessToken == null) {
            return "redirect:/login";
        }

        String userEmail = getCurrentUserEmail(authentication);

        // Lấy lịch bận + gọi AI + preview (giữ nguyên logic cũ)
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime end = now.plusDays(7);

        List<com.google.api.services.calendar.model.TimePeriod> busySlots =
                googleCalendarService.getBusySlots(accessToken, now, end);

        String busyContext = convertBusySlotsToString(busySlots);

        PlanResponse plan = taskAIService.generatePlan(description, busyContext);

        model.addAttribute("plan", plan);
        model.addAttribute("originalDescription", description);

        System.out.println(">>> SẼ CHUYỂN ĐẾN TRANG PREVIEW");
        return "task-preview";
    }

    @PostMapping("/tasks/confirm")
    public String confirmPlan(
            @ModelAttribute("plan") PlanResponse plan,
            @RequestParam("originalDescription") String description,
            Authentication authentication,   // ← KHÔNG dùng @AuthenticationPrincipal
            RedirectAttributes redirectAttributes) {

        // Kiểm tra authentication theo đúng cách bạn yêu cầu
        if (authentication == null || !(authentication instanceof OAuth2AuthenticationToken)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chưa đăng nhập!");
            return "redirect:/login";
        }

        try {
            String accessToken = getGoogleAccessToken(authentication);
            String userEmail = getCurrentUserEmail(authentication);

            // === Phần lưu DB và Google Calendar giữ nguyên ===
            Task newTask = new Task();
            newTask.setUserId(1); // TODO: sau này dùng userEmail để lấy real userId
            newTask.setDescription(description);
            newTask.setStatus("IN_PROGRESS");
            newTask.setCreatedAt(LocalDateTime.now());

            taskMapper.insert(newTask);
            int taskId = newTask.getId();

            if (plan.getTodos() != null) {
                for (PlanResponse.TodoItem todo : plan.getTodos()) {
                    SubTask sub = new SubTask();
                    sub.setTaskId(taskId);
                    sub.setTitle(todo.getTitle());
                    sub.setIsCompleted(false);
                    subTaskMapper.insert(sub);
                }
            }

            if (plan.getSchedules() != null && accessToken != null) {
                for (PlanResponse.ScheduleItem item : plan.getSchedules()) {
                    String googleEventId = googleCalendarService.createEvent(
                            accessToken,
                            item.getTitle(),
                            item.getStartTime().atZone(ZoneId.systemDefault()),
                            item.getEndTime().atZone(ZoneId.systemDefault())
                    );

                    Schedule schedule = new Schedule();
                    schedule.setTaskId(taskId);
                    schedule.setTitle(item.getTitle());
                    schedule.setStartTime(item.getStartTime());
                    schedule.setEndTime(item.getEndTime());
                    schedule.setGoogleEventId(googleEventId);

                    scheduleMapper.insert(schedule);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đã lên kế hoạch và đồng bộ lịch thành công!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/tasks/new";
        }
    }

    private String convertBusySlotsToString(List<com.google.api.services.calendar.model.TimePeriod> busySlots) {
        if (busySlots == null || busySlots.isEmpty()) {
            return "Bạn hoàn toàn rảnh trong thời gian này.";
        }

        StringBuilder sb = new StringBuilder("Bạn bận vào các khung giờ sau: ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");

        for (com.google.api.services.calendar.model.TimePeriod period : busySlots) {
            Instant startInstant = Instant.parse(period.getStart().toString());
            LocalDateTime start = LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault());
            sb.append(start.format(formatter)).append(", ");
        }
        return sb.toString();
    }
}