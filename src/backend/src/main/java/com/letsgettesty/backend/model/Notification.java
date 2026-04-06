package com.letsgettesty.backend.model;

import java.time.LocalDateTime;

public class Notification {

    private int id;
    private int reservationId;
    private NotificationChannel channel;
    private String destination;
    private String subject;
    private String message;
    private LocalDateTime sentAt;

    public Notification() {
    }

    public Notification(
            int id,
            int reservationId,
            NotificationChannel channel,
            String destination,
            String subject,
            String message,
            LocalDateTime sentAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.channel = channel;
        this.destination = destination;
        this.subject = subject;
        this.message = message;
        this.sentAt = sentAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
