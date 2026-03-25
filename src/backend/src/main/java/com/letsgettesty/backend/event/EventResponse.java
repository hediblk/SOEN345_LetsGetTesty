package com.letsgettesty.backend.event;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.EventCategory;

public record EventResponse(
        int id,
        String title,
        String description,
        EventCategory category,
        String location,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        int capacity,
        int price,
        int reservedCount,
        @JsonProperty("isCancelled") boolean cancelled) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory(),
                event.getLocation(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getCapacity(),
                event.getPrice(),
                event.getReservedCount(),
                event.isCancelled());
    }
}
