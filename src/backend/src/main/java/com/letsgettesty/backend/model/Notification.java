package com.letsgettesty.backend.model;

import java.time.LocalDateTime;

public class Notification {

    private int id;
    private NotificationChannel channel;
    private String destination;
    private String message;
    private LocalDateTime sentAt;

    public Notification() {
    }

    public Notification(
            int id,
            NotificationChannel channel,
            String destination,
            String message,
            LocalDateTime sentAt) {
        this.id = id;
        this.channel = channel;
        this.destination = destination;
        this.message = message;
        this.sentAt = sentAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
