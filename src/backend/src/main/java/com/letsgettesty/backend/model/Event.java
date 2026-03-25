package com.letsgettesty.backend.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Event {

    private int id;
    private String title;
    private String description;
    private EventCategory category;
    private String location;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private int capacity;
    private int reservedCount;
    private boolean isCancelled;

    public Event() {
    }

    public Event(
            int id,
            String title,
            String description,
            EventCategory category,
            String location,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            int capacity,
            int reservedCount,
            boolean isCancelled) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.capacity = capacity;
        this.reservedCount = reservedCount;
        this.isCancelled = isCancelled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventCategory getCategory() {
        return category;
    }

    public void setCategory(EventCategory category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getReservedCount() {
        return reservedCount;
    }

    public void setReservedCount(int reservedCount) {
        this.reservedCount = reservedCount;
    }

    @JsonProperty("isCancelled")
    public boolean isCancelled() {
        return isCancelled;
    }

    @JsonProperty("isCancelled")
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
