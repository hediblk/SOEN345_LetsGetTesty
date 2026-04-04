package com.letsgettesty.backend.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.EventCategory;

class EventResponseTest {

    @Test
    void fromMapsAllFieldsIncludingCancellationFlag() {
        Event event = new Event(
                42,
                "Gala",
                "Evening wear",
                EventCategory.OTHERS,
                "Grand Hall",
                LocalDateTime.parse("2026-12-31T21:00:00"),
                LocalDateTime.parse("2027-01-01T02:00:00"),
                200,
                75,
                33,
                true);

        EventResponse response = EventResponse.from(event);

        assertThat(response.id()).isEqualTo(42);
        assertThat(response.title()).isEqualTo("Gala");
        assertThat(response.description()).isEqualTo("Evening wear");
        assertThat(response.category()).isEqualTo(EventCategory.OTHERS);
        assertThat(response.location()).isEqualTo("Grand Hall");
        assertThat(response.startsAt()).isEqualTo(LocalDateTime.parse("2026-12-31T21:00:00"));
        assertThat(response.endsAt()).isEqualTo(LocalDateTime.parse("2027-01-01T02:00:00"));
        assertThat(response.capacity()).isEqualTo(200);
        assertThat(response.price()).isEqualTo(75);
        assertThat(response.reservedCount()).isEqualTo(33);
        assertThat(response.cancelled()).isTrue();
    }
}
