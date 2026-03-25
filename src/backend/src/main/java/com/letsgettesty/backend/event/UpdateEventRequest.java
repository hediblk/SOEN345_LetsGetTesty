package com.letsgettesty.backend.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.letsgettesty.backend.model.EventCategory;

public record UpdateEventRequest(
        String title,
        String description,
        EventCategory category,
        String location,
        LocalDateTime startsAt,
        @JsonProperty("date") @JsonAlias("eventDate") LocalDate date,
        LocalDateTime endsAt,
        Integer capacity,
        Integer price) {

    public EventRequest toEventRequest() {
        LocalDateTime effectiveStartsAt = startsAt;
        if (effectiveStartsAt == null && date != null) {
            effectiveStartsAt = date.atTime(18, 0);
        }
        return new EventRequest(title, description, category, location, effectiveStartsAt, endsAt, capacity, price);
    }
}
