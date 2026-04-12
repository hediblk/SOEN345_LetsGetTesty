package com.letsgettesty.backend.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.EventCategory;

class JdbcEventRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final JdbcEventRepository repository = new JdbcEventRepository(jdbcTemplate);

    private Event sampleEvent() {
        return new Event(1, "Concert", "Desc", EventCategory.CONCERT, "Venue",
                LocalDateTime.parse("2026-06-01T18:00:00"), null, 100, 20, 5, false);
    }

    // --- findAllOrderedByStartsAt ---

    @Test
    @SuppressWarnings("unchecked")
    void findAllOrderedByStartsAtReturnsEvents() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of(sampleEvent()));

        List<Event> result = repository.findAllOrderedByStartsAt();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Concert");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAllOrderedByStartsAtReturnsEmptyList() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

        List<Event> result = repository.findAllOrderedByStartsAt();

        assertThat(result).isEmpty();
    }

    // --- findById ---

    @Test
    @SuppressWarnings("unchecked")
    void findByIdReturnsEventWhenFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1))).thenReturn(List.of(sampleEvent()));

        Optional<Event> result = repository.findById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getTitle()).isEqualTo("Concert");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByIdReturnsEmptyOptionalWhenNotFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(999))).thenReturn(List.of());

        Optional<Event> result = repository.findById(999);

        assertThat(result).isEmpty();
    }

    // --- insert ---

    @Test
    @SuppressWarnings("unchecked")
    void insertDelegatesToJdbcTemplateAndReturnsEvent() {
        Event event = sampleEvent();
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(event);

        Event result = repository.insert(event);

        assertThat(result).isEqualTo(event);
        assertThat(result.getTitle()).isEqualTo("Concert");
    }

    // --- update ---

    @Test
    void updateReturnsTrueWhenRowIsUpdated() {
        when(jdbcTemplate.update(anyString(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        boolean result = repository.update(sampleEvent());

        assertThat(result).isTrue();
    }

    @Test
    void updateReturnsFalseWhenNoRowUpdated() {
        when(jdbcTemplate.update(anyString(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        boolean result = repository.update(sampleEvent());

        assertThat(result).isFalse();
    }

    // --- setCancelled ---

    @Test
    void setCancelledReturnsTrueWhenRowIsUpdated() {
        when(jdbcTemplate.update(anyString(), eq(true), eq(1))).thenReturn(1);

        boolean result = repository.setCancelled(1, true);

        assertThat(result).isTrue();
    }

    @Test
    void setCancelledReturnsFalseWhenNoRowUpdated() {
        when(jdbcTemplate.update(anyString(), eq(false), eq(999))).thenReturn(0);

        boolean result = repository.setCancelled(999, false);

        assertThat(result).isFalse();
    }
}
