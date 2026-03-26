package com.letsgettesty.backend.reservation;

import java.util.List;
import java.util.Optional;

import com.letsgettesty.backend.model.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(int id);

    List<Reservation> findByUserId(int userId);

    List<Reservation> findByEventId(int eventId);

    Reservation insert(Reservation reservation);

    boolean setCancelled(int id);

    boolean exists(int userId, int eventId);
}