package com.letsgettesty.backend.reservation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.letsgettesty.backend.event.EventRepository;
import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.Reservation;
import com.letsgettesty.backend.model.ReservationStatus;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    public ReservationService(ReservationRepository reservationRepository, EventRepository eventRepository) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
    }

    public List<Reservation> listReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservation(int id) {
        return reservationRepository
                .findById(id)
                .orElseThrow(() -> notFound("Reservation not found."));
    }

    public List<Reservation> listByUser(int userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> listByEvent(int eventId) {
        return reservationRepository.findByEventId(eventId);
    }

    public Reservation createReservation(int userId, int eventId) {
        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> notFound("Event not found."));

        if (event.isCancelled()) {
            throw badRequest("Cannot reserve a cancelled event.");
        }
        if (event.getReservedCount() >= event.getCapacity()) {
            throw badRequest("Event is fully booked.");
        }
        if (reservationRepository.exists(userId, eventId)) {
            throw badRequest("User already has a reservation for this event.");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setEventId(eventId);

        Reservation created = reservationRepository.insert(reservation);

        event.setReservedCount(event.getReservedCount() + 1);
        eventRepository.update(event);

        return created;
    }

    public Reservation cancelReservation(int id) {
        Reservation existing = getReservation(id);

        if (existing.getStatus() == ReservationStatus.CANCELLED) {
            return existing;
        }

        if (!reservationRepository.setCancelled(id)) {
            throw notFound("Reservation not found.");
        }

        eventRepository.findById(existing.getEventId()).ifPresent(event -> {
            event.setReservedCount(Math.max(0, event.getReservedCount() - 1));
            eventRepository.update(event);
        });

        return getReservation(id);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}