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

import com.letsgettesty.backend.auth.AccountRole;
import com.letsgettesty.backend.auth.AuthorizationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    private final EventService eventService;
    private final AuthorizationService authorizationService;

    public EventController(EventService eventService, AuthorizationService authorizationService) {
        this.eventService = eventService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<EventResponse> listEvents(HttpServletRequest request) {
        authorizationService.requireAnyRole(request, AccountRole.USER, AccountRole.ADMIN);
        return eventService.listEvents().stream().map(EventResponse::from).toList();
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable int id, HttpServletRequest request) {
        authorizationService.requireAnyRole(request, AccountRole.USER, AccountRole.ADMIN);
        return EventResponse.from(eventService.getEvent(id));
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest eventRequest, HttpServletRequest request) {
        authorizationService.requireRole(request, AccountRole.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(eventService.createEvent(eventRequest)));
    }

    @PutMapping("/{id}")
    public EventResponse replaceEvent(@PathVariable int id, @RequestBody UpdateEventRequest eventRequest, HttpServletRequest request) {
        authorizationService.requireRole(request, AccountRole.ADMIN);
        return EventResponse.from(eventService.replaceEvent(id, eventRequest));
    }

    @PatchMapping("/{id}/cancel")
    public EventResponse cancelEvent(@PathVariable int id, HttpServletRequest request) {
        authorizationService.requireRole(request, AccountRole.ADMIN);
        return EventResponse.from(eventService.cancelEvent(id));
    }
}
