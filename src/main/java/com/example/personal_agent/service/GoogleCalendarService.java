package com.example.personal_agent.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class GoogleCalendarService {

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    private Calendar getCalendarService(com.google.api.client.auth.oauth2.Credential credential) throws IOException, GeneralSecurityException {
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName("AI Agent")
                .build();
    }

    // Lấy lịch bận
    public List<TimePeriod> getBusySlots(String accessToken, ZonedDateTime start, ZonedDateTime end) {
        try {
            com.google.api.client.auth.oauth2.Credential credential = createCredential(accessToken);
            Calendar service = getCalendarService(credential);

            FreeBusyRequest request = new FreeBusyRequest();
            request.setItems(Collections.singletonList(new FreeBusyRequestItem().setId("primary")));
            request.setTimeMin(new DateTime(start.toInstant().toString()));
            request.setTimeMax(new DateTime(end.toInstant().toString()));

            FreeBusyResponse response = service.freebusy().query(request).execute();

            List<TimePeriod> busySlots = new ArrayList<>();
            for (FreeBusyCalendar  calendar : response.getCalendars().values()) {
                if (calendar.getBusy() != null) {
                    busySlots.addAll(calendar.getBusy());
                }
            }
            return busySlots;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Tạo sự kiện
    public String createEvent(String accessToken, String title, ZonedDateTime start, ZonedDateTime end) {
        try {
            com.google.api.client.auth.oauth2.Credential credential = createCredential(accessToken);
            Calendar service = getCalendarService(credential);

            Event event = new Event()
                    .setSummary(title)
                    .setDescription("Tự động tạo bởi AI Agent");

            EventDateTime startEvent = new EventDateTime()
                    .setDateTime(new DateTime(start.toInstant().toString()))
                    .setTimeZone("Asia/Ho_Chi_Minh");
            event.setStart(startEvent);

            EventDateTime endEvent = new EventDateTime()
                    .setDateTime(new DateTime(end.toInstant().toString()))
                    .setTimeZone("Asia/Ho_Chi_Minh");
            event.setEnd(endEvent);

            // Cài đặt Reminder
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(
                            new EventReminder().setMethod("email").setMinutes(1440), // 1 ngày
                            new EventReminder().setMethod("popup").setMinutes(15)
                    ));
            event.setReminders(reminders);

            Event createdEvent = service.events().insert("primary", event).execute();
            return createdEvent.getId();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private com.google.api.client.auth.oauth2.Credential createCredential(String accessToken) {
        return new com.google.api.client.auth.oauth2.Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(accessToken);
    }
}