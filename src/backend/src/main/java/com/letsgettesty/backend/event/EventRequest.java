package com.letsgettesty.backend.event;

import java.time.LocalDateTime;

import com.letsgettesty.backend.model.EventCategory;

public record EventRequest(
        String title,
        String description,
        EventCategory category,
        String location,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Integer capacity) {
}
