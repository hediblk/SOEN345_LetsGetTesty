package com.letsgettesty.backend.event;

import java.util.List;
import java.util.Optional;

import com.letsgettesty.backend.model.Event;

public interface EventRepository {

    List<Event> findAllOrderedByStartsAt();

    Optional<Event> findById(int id);

    Event insert(Event event);


    boolean update(Event event);

    boolean setCancelled(int id, boolean cancelled);
}
