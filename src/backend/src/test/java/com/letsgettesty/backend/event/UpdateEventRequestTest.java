package com.letsgettesty.backend.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.letsgettesty.backend.model.EventCategory;

class UpdateEventRequestTest {

    @Test
    void toEventRequestPrefersStartsAtOverDate() {
        LocalDateTime startsAt = LocalDateTime.parse("2026-03-01T10:30:00");
        UpdateEventRequest update =
                new UpdateEventRequest("T", null, EventCategory.MOVIE, "L", startsAt, LocalDate.parse("2026-06-06"), null, 10, 0);

        EventRequest request = update.toEventRequest();

        assertThat(request.startsAt()).isEqualTo(startsAt);
    }

    @Test
    void toEventRequestUsesDateAt1800WhenStartsAtNull() {
        UpdateEventRequest update =
                new UpdateEventRequest(
                        "T",
                        null,
                        EventCategory.CONCERT,
                        "Hall",
                        null,
                        LocalDate.parse("2026-08-15"),
                        null,
                        20,
                        5);

        EventRequest request = update.toEventRequest();

        assertThat(request.startsAt()).isEqualTo(LocalDate.parse("2026-08-15").atTime(18, 0));
    }

    @Test
    void toEventRequestLeavesStartsAtNullWhenBothMissing() {
        UpdateEventRequest update =
                new UpdateEventRequest("T", null, EventCategory.SPORT, "Field", null, null, null, 30, 0);

        EventRequest request = update.toEventRequest();

        assertThat(request.startsAt()).isNull();
    }
}
