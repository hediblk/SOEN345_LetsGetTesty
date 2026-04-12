package com.letsgettesty.backend.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.letsgettesty.backend.model.Notification;
import com.letsgettesty.backend.model.NotificationChannel;

class JdbcNotificationRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final JdbcNotificationRepository repository = new JdbcNotificationRepository(jdbcTemplate);

    private Notification sampleNotification() {
        return new Notification(
                0,
                5,
                NotificationChannel.EMAIL,
                "jane@example.com",
                "Registration confirmed",
                "You have been registered for Concert.",
                LocalDateTime.parse("2026-01-01T10:00:00"));
    }

    // --- insert ---

    @Test
    @SuppressWarnings("unchecked")
    void insertReturnsPersistedNotificationWithSubjectRestored() {
        Notification input = sampleNotification();
        Notification saved = new Notification(
                99,
                5,
                NotificationChannel.EMAIL,
                "jane@example.com",
                null,
                "You have been registered for Concert.",
                LocalDateTime.parse("2026-01-01T10:00:00"));

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class),
                eq(5),
                eq("email"),
                eq("jane@example.com"),
                eq("You have been registered for Concert."),
                eq(LocalDateTime.parse("2026-01-01T10:00:00"))))
                .thenReturn(saved);

        Notification result = repository.insert(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(99);
        assertThat(result.getReservationId()).isEqualTo(5);
        assertThat(result.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(result.getDestination()).isEqualTo("jane@example.com");
        assertThat(result.getMessage()).isEqualTo("You have been registered for Concert.");
        assertThat(result.getSubject()).isEqualTo("Registration confirmed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void insertRestoresSubjectFromInputWhenSavedHasNullSubject() {
        Notification input = new Notification(
                0, 3, NotificationChannel.EMAIL, "user@example.com",
                "Event update", "Your event has been updated.", LocalDateTime.now());
        Notification saved = new Notification(
                10, 3, NotificationChannel.EMAIL, "user@example.com",
                null, "Your event has been updated.", LocalDateTime.now());

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class),
                any(), any(), any(), any(), any()))
                .thenReturn(saved);

        Notification result = repository.insert(input);

        assertThat(result.getSubject()).isEqualTo("Event update");
    }

    @Test
    @SuppressWarnings("unchecked")
    void insertWithSmsChannelUsesCorrectPersistenceValue() {
        Notification input = new Notification(
                0, 7, NotificationChannel.SMS, "514-555-0100",
                null, "Your reservation is confirmed.", LocalDateTime.parse("2026-02-01T09:00:00"));
        Notification saved = new Notification(
                20, 7, NotificationChannel.SMS, "514-555-0100",
                null, "Your reservation is confirmed.", LocalDateTime.parse("2026-02-01T09:00:00"));

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class),
                eq(7),
                eq("sms"),
                eq("514-555-0100"),
                eq("Your reservation is confirmed."),
                eq(LocalDateTime.parse("2026-02-01T09:00:00"))))
                .thenReturn(saved);

        Notification result = repository.insert(input);

        assertThat(result.getId()).isEqualTo(20);
        assertThat(result.getChannel()).isEqualTo(NotificationChannel.SMS);
    }
}
