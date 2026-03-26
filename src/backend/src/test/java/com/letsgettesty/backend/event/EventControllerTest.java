package com.letsgettesty.backend.event;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.letsgettesty.backend.auth.AuthExceptionHandler;
import com.letsgettesty.backend.auth.JwtAuthInterceptor;
import com.letsgettesty.backend.auth.JwtService;
import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.EventCategory;

@WebMvcTest(EventController.class)
@Import({ AuthExceptionHandler.class, JwtAuthInterceptor.class })
class EventControllerTest {

    private static final String VALID_TOKEN = "valid-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        doNothing().when(jwtService).verifyToken(VALID_TOKEN);
    }

    @Test
    void listEventsReturnsJsonArray() throws Exception {
        when(eventService.listEvents())
                .thenReturn(
                        List.of(
                                new Event(
                                        1,
                                        "Dune",
                                        null,
                                        EventCategory.MOVIE,
                                        "Cinema",
                                        LocalDateTime.parse("2026-04-15T18:00"),
                                        null,
                                        280,
                                        22,
                                        0,
                                        false)));

        mockMvc.perform(get("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$[0].price").value(22));
    }

    @Test
    void listEventsRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", equalTo("Missing bearer token.")));
    }

    @Test
    void createEventReturns201() throws Exception {
        Event created = new Event(
                9,
                "New Show",
                null,
                EventCategory.CONCERT,
                "Hall",
                LocalDateTime.parse("2026-07-01T18:00"),
                null,
                40,
                12,
                0,
                false);
        when(eventService.createEvent(any(EventRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "title": "New Show",
                                  "description": null,
                                  "category": "CONCERT",
                                  "location": "Hall",
                                  "startsAt": "2026-07-01T18:00:00",
                                  "endsAt": null,
                                  "capacity": 40,
                                  "price": 12
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.price").value(12));
    }

    @Test
    void getEventReturnsHandledNotFound() throws Exception {
        when(eventService.getEvent(99)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

        mockMvc.perform(get("/api/events/99")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", equalTo("Event not found.")));
    }

    @Test
    void getEventByIdReturnsOk() throws Exception {
        Event event = new Event(
                2,
                "Jazz Night",
                null,
                EventCategory.CONCERT,
                "Hall",
                LocalDateTime.parse("2026-04-18T19:30"),
                LocalDateTime.parse("2026-04-18T22:00"),
                118,
                45,
                2,
                false);
        when(eventService.getEvent(2)).thenReturn(event);

        mockMvc.perform(get("/api/events/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("Jazz Night")))
                .andExpect(jsonPath("$.reservedCount").value(2))
                .andExpect(jsonPath("$.isCancelled").value(false));
    }

    @Test
    void replaceEventReturnsOk() throws Exception {
        Event updated = new Event(
                1,
                "Updated",
                null,
                EventCategory.MOVIE,
                "Cinema",
                LocalDateTime.parse("2026-04-15T18:00"),
                null,
                100,
                20,
                0,
                false);
        when(eventService.replaceEvent(eq(1), any(UpdateEventRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/events/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "title": "Updated",
                                  "description": null,
                                  "category": "MOVIE",
                                  "location": "Cinema",
                                  "startsAt": "2026-04-15T18:00:00",
                                  "endsAt": null,
                                  "capacity": 100,
                                  "price": 20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("Updated")))
                .andExpect(jsonPath("$.capacity").value(100));

        verify(eventService).replaceEvent(eq(1), any(UpdateEventRequest.class));
    }

    @Test
    void replaceEventReturnsNotFound() throws Exception {
        when(eventService.replaceEvent(eq(42), any(UpdateEventRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

        mockMvc.perform(put("/api/events/42")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "title": "X",
                                  "description": null,
                                  "category": "MOVIE",
                                  "location": "Here",
                                  "startsAt": "2026-09-01T12:00:00",
                                  "endsAt": null,
                                  "capacity": 10,
                                  "price": 0
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", equalTo("Event not found.")));
    }

    @Test
    void replaceEventReturnsBadRequestWhenCancelled() throws Exception {
        when(eventService.replaceEvent(eq(5), any(UpdateEventRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelled events cannot be updated."));

        mockMvc.perform(put("/api/events/5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "title": "Try",
                                  "description": null,
                                  "category": "CONCERT",
                                  "location": "Hall",
                                  "startsAt": "2026-10-01T18:00:00",
                                  "endsAt": null,
                                  "capacity": 50,
                                  "price": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Cancelled events cannot be updated.")));
    }

    @Test
    void replaceEventAcceptsDateFieldInsteadOfStartsAt() throws Exception {
        Event updated = new Event(
                6,
                "Day only",
                null,
                EventCategory.SPORT,
                "Field",
                LocalDateTime.parse("2026-11-20T18:00"),
                null,
                30,
                5,
                0,
                false);
        when(eventService.replaceEvent(eq(6), any(UpdateEventRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/events/6")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "title": "Day only",
                                  "description": null,
                                  "category": "SPORT",
                                  "location": "Field",
                                  "date": "2026-11-20",
                                  "endsAt": null,
                                  "capacity": 30,
                                  "price": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("Day only")));

        verify(eventService).replaceEvent(eq(6), any(UpdateEventRequest.class));
    }

    @Test
    void cancelEventReturnsOk() throws Exception {
        Event cancelled = new Event(
                3,
                "Show",
                null,
                EventCategory.SPORT,
                "Arena",
                LocalDateTime.parse("2026-05-01T18:00"),
                null,
                200,
                50,
                0,
                true);
        when(eventService.cancelEvent(3)).thenReturn(cancelled);

        mockMvc.perform(patch("/api/events/3/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCancelled").value(true));

        verify(eventService).cancelEvent(3);
    }

    @Test
    void cancelEventReturnsHandledNotFound() throws Exception {
        when(eventService.cancelEvent(99))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

        mockMvc.perform(patch("/api/events/99/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", equalTo("Event not found.")));

        verify(eventService).cancelEvent(99);
    }

    @Test
    void replaceEventReturnsHandledBadRequestWhenLocationMissing() throws Exception {
        when(eventService.replaceEvent(eq(4), any(UpdateEventRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location is required."));

        mockMvc.perform(put("/api/events/4")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "title": "Updated",
                                  "description": null,
                                  "category": "MOVIE",
                                  "location": "",
                                  "startsAt": "2026-04-15T18:00:00",
                                  "endsAt": null,
                                  "capacity": 100,
                                  "price": 20
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Location is required.")));

        verify(eventService).replaceEvent(eq(4), any(UpdateEventRequest.class));
    }

    @Test
    void createEventReturnsHandledBadRequest() throws Exception {
        when(eventService.createEvent(any(EventRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required."));

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "title": "",
                                  "description": null,
                                  "category": "MOVIE",
                                  "location": "Here",
                                  "startsAt": "2026-08-01T12:00:00",
                                  "endsAt": null,
                                  "capacity": 10,
                                  "price": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Title is required.")));
    }
}
