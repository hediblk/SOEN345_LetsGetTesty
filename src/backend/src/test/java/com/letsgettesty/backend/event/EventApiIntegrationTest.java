package com.letsgettesty.backend.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsgettesty.backend.auth.AccountRecord;
import com.letsgettesty.backend.auth.AccountRole;
import com.letsgettesty.backend.auth.JwtService;
import com.letsgettesty.backend.model.EventCategory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class EventApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void listEventsAsUserReturnsSeededEvents() throws Exception {
        mockMvc.perform(get("/api/events").header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))))
                .andExpect(jsonPath("$[*].title", hasItem(containsString("Dune"))));
    }

    @Test
    void listEventsWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing bearer token."));
    }

    @Test
    void listEventsWithInvalidBearerReturns401() throws Exception {
        mockMvc.perform(get("/api/events").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid bearer token."));
    }

    @Test
    void getEventByIdAsUserReturnsEvent() throws Exception {
        var row = jdbcTemplate.queryForMap(
                """
                SELECT id, title, trim(both from category::text) AS category
                FROM events
                WHERE COALESCE(is_cancelled, false) = false
                ORDER BY id
                LIMIT 1
                """);
        assertThat(row).isNotEmpty();

        int id = ((Number) row.get("id")).intValue();
        String title = (String) row.get("title");
        String category = (String) row.get("category");

        mockMvc.perform(get("/api/events/" + id).header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.category").value(category))
                .andExpect(jsonPath("$.isCancelled").value(false));
    }

    @Test
    void getEventNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/events/99999").header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found."));
    }

    @Test
    void createEventAsAdminReturns201AndPersists() throws Exception {
        EventRequest body = new EventRequest(
                "API Integration Concert",
                "Created via MockMvc",
                EventCategory.CONCERT,
                "Test Hall",
                LocalDateTime.parse("2026-08-01T20:00"),
                null,
                80,
                55);

        String response = mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("API Integration Concert"))
                .andExpect(jsonPath("$.reservedCount").value(0))
                .andExpect(jsonPath("$.isCancelled").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        int id = objectMapper.readTree(response).get("id").asInt();
        Long rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events WHERE id = ? AND title = ?", Long.class, id, "API Integration Concert");
        assertThat(rows).isEqualTo(1L);
    }

    @Test
    void replaceEventAsUserReturns403() throws Exception {
        UpdateEventRequest update = new UpdateEventRequest(
                "User Cannot Put",
                null,
                EventCategory.MOVIE,
                "Somewhere",
                LocalDateTime.parse("2026-12-10T12:00"),
                null,
                null,
                20,
                8);

        mockMvc.perform(put("/api/events/3")
                        .header(HttpHeaders.AUTHORIZATION, userBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource."));
    }

    @Test
    void replaceNonExistingEventReturns404() throws Exception {
        UpdateEventRequest update = new UpdateEventRequest(
                "Ghost Event",
                null,
                EventCategory.CONCERT,
                "Venue",
                LocalDateTime.parse("2027-01-01T18:00"),
                null,
                null,
                50,
                20);

        mockMvc.perform(put("/api/events/99997")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found."));
    }

    @Test
    void replaceCancelledEventReturns400() throws Exception {
        EventRequest create = new EventRequest(
                "Cancelled Then Replace",
                null,
                EventCategory.SPORT,
                "Arena",
                LocalDateTime.parse("2027-02-01T19:00"),
                null,
                40,
                12);

        String createJson = mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int id = objectMapper.readTree(createJson).get("id").asInt();

        mockMvc.perform(patch("/api/events/" + id + "/cancel").header(HttpHeaders.AUTHORIZATION, adminBearer()))
                .andExpect(status().isOk());

        UpdateEventRequest update = new UpdateEventRequest(
                "Should Not Apply",
                null,
                EventCategory.SPORT,
                "Arena",
                LocalDateTime.parse("2027-02-01T19:00"),
                null,
                null,
                45,
                12);

        mockMvc.perform(put("/api/events/" + id)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cancelled events cannot be updated."));
    }

    @Test
    void createEventAsUserReturns403() throws Exception {
        EventRequest body = new EventRequest(
                "Should Fail",
                null,
                EventCategory.MOVIE,
                "Somewhere",
                LocalDateTime.parse("2026-09-01T12:00"),
                null,
                10,
                5);

        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, userBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource."));
    }

    @Test
    void replaceEventAsAdminUpdatesRow() throws Exception {
        // Event 6: no reservations in seed; safe to replace without breaking FK tests.
        UpdateEventRequest update = new UpdateEventRequest(
                "Blade Runner (updated via API)",
                null,
                EventCategory.MOVIE,
                "Cineplex Forum, Montreal",
                LocalDateTime.parse("2026-05-12T18:00"),
                null,
                LocalDateTime.parse("2026-05-12T21:00"),
                160,
                18);

        mockMvc.perform(put("/api/events/6")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.title").value("Blade Runner (updated via API)"))
                .andExpect(jsonPath("$.capacity").value(160));

        String title = jdbcTemplate.queryForObject("SELECT title FROM events WHERE id = 6", String.class);
        assertThat(title).isEqualTo("Blade Runner (updated via API)");
    }

    @Test
    void replaceEventWithCapacityBelowReservedReturns400() throws Exception {
        EventRequest create = new EventRequest(
                "Capacity Constraint Event",
                null,
                EventCategory.MOVIE,
                "Cinema",
                LocalDateTime.parse("2026-11-05T19:00"),
                null,
                100,
                15);

        String createJson = mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int id = objectMapper.readTree(createJson).get("id").asInt();
        jdbcTemplate.update("UPDATE events SET reserved_count = 4 WHERE id = ?", id);

        UpdateEventRequest update = new UpdateEventRequest(
                "Capacity Constraint Event",
                null,
                EventCategory.MOVIE,
                "Cinema",
                LocalDateTime.parse("2026-11-05T19:00"),
                null,
                LocalDateTime.parse("2026-11-05T21:30"),
                2,
                15);

        mockMvc.perform(put("/api/events/" + id)
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Capacity cannot be less than the number of reserved seats."));
    }

    @Test
    void cancelEventAsAdminMarksCancelled() throws Exception {
        EventRequest body = new EventRequest(
                "To Cancel Via PATCH",
                null,
                EventCategory.SPORT,
                "Temp Venue",
                LocalDateTime.parse("2026-10-01T15:00"),
                null,
                30,
                10);

        String createJson = mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int newId = objectMapper.readTree(createJson).get("id").asInt();

        mockMvc.perform(patch("/api/events/" + newId + "/cancel").header(HttpHeaders.AUTHORIZATION, adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newId))
                .andExpect(jsonPath("$.isCancelled").value(true));

        Boolean cancelled = jdbcTemplate.queryForObject(
                "SELECT is_cancelled FROM events WHERE id = ?", Boolean.class, newId);
        assertThat(cancelled).isTrue();
    }

    @Test
    void cancelNonExistingEventReturns404() throws Exception {
        mockMvc.perform(patch("/api/events/99998/cancel").header(HttpHeaders.AUTHORIZATION, adminBearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Event not found."));
    }

    @Test
    void cancelAlreadyCancelledEventReturnsOkAndStaysCancelled() throws Exception {
        EventRequest body = new EventRequest(
                "Double Cancel Target",
                null,
                EventCategory.MOVIE,
                "Theatre",
                LocalDateTime.parse("2027-03-01T20:00"),
                null,
                25,
                7);

        String createJson = mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, adminBearer())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int id = objectMapper.readTree(createJson).get("id").asInt();

        mockMvc.perform(patch("/api/events/" + id + "/cancel").header(HttpHeaders.AUTHORIZATION, adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCancelled").value(true));

        mockMvc.perform(patch("/api/events/" + id + "/cancel").header(HttpHeaders.AUTHORIZATION, adminBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.isCancelled").value(true));
    }

    @Test
    void cancelEventAsUserReturns403() throws Exception {
        mockMvc.perform(patch("/api/events/1/cancel").header(HttpHeaders.AUTHORIZATION, userBearer()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource."));
    }

    private String adminBearer() {
        return "Bearer "
                + jwtService.generateToken(
                        new AccountRecord(1, AccountRole.ADMIN, "Admin One", "admin123", "admin1@example.com", null));
    }

    private String userBearer() {
        return "Bearer "
                + jwtService.generateToken(
                        new AccountRecord(2, AccountRole.USER, "user1", "user123", "user1@example.com", null));
    }
}
