package com.letsgettesty.backend.event;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventResponse> listEvents() {
        return eventService.listEvents().stream().map(EventResponse::from).toList();
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable int id) {
        return EventResponse.from(eventService.getEvent(id));
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(eventService.createEvent(request)));
    }

    @PutMapping("/{id}")
    public EventResponse replaceEvent(@PathVariable int id, @RequestBody UpdateEventRequest request) {
        return EventResponse.from(eventService.replaceEvent(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public EventResponse cancelEvent(@PathVariable int id) {
        return EventResponse.from(eventService.cancelEvent(id));
    }
}
