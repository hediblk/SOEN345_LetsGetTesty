package com.letsgettesty.backend.model;

import java.time.LocalDateTime;

public class Reservation {

    private int id;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    public Reservation() {
    }

    public Reservation(int id, ReservationStatus status, LocalDateTime createdAt, LocalDateTime cancelledAt) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
