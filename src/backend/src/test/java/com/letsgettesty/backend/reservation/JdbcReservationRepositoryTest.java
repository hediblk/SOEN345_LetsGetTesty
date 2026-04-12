package com.letsgettesty.backend.reservation;

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

import com.letsgettesty.backend.model.Reservation;
import com.letsgettesty.backend.model.ReservationStatus;

class JdbcReservationRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final JdbcReservationRepository repository = new JdbcReservationRepository(jdbcTemplate);

    private Reservation sampleReservation() {
        return new Reservation(1, 2, 10, ReservationStatus.CONFIRMED,
                LocalDateTime.parse("2026-01-01T10:00:00"), null);
    }

    // --- findAll ---

    @Test
    @SuppressWarnings("unchecked")
    void findAllReturnsAllReservations() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of(sampleReservation()));

        List<Reservation> result = repository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAllReturnsEmptyList() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

        List<Reservation> result = repository.findAll();

        assertThat(result).isEmpty();
    }

    // --- findById ---

    @Test
    @SuppressWarnings("unchecked")
    void findByIdReturnsReservationWhenFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1))).thenReturn(List.of(sampleReservation()));

        Optional<Reservation> result = repository.findById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByIdReturnsEmptyOptionalWhenNotFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(999))).thenReturn(List.of());

        Optional<Reservation> result = repository.findById(999);

        assertThat(result).isEmpty();
    }

    // --- findByUserId ---

    @Test
    @SuppressWarnings("unchecked")
    void findByUserIdReturnsReservationsForUser() {
        Reservation r1 = new Reservation(1, 2, 10, ReservationStatus.CONFIRMED,
                LocalDateTime.parse("2026-01-01T10:00:00"), null);
        Reservation r2 = new Reservation(2, 2, 11, ReservationStatus.CONFIRMED,
                LocalDateTime.parse("2026-01-02T10:00:00"), null);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(2))).thenReturn(List.of(r1, r2));

        List<Reservation> result = repository.findByUserId(2);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getUserId() == 2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByUserIdReturnsEmptyWhenNoReservations() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(99))).thenReturn(List.of());

        List<Reservation> result = repository.findByUserId(99);

        assertThat(result).isEmpty();
    }

    // --- findByEventId ---

    @Test
    @SuppressWarnings("unchecked")
    void findByEventIdReturnsReservationsForEvent() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(10))).thenReturn(List.of(sampleReservation()));

        List<Reservation> result = repository.findByEventId(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventId()).isEqualTo(10);
    }

    // --- insert ---

    @Test
    @SuppressWarnings("unchecked")
    void insertDelegatesToJdbcTemplateAndReturnsReservation() {
        Reservation reservation = sampleReservation();
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(2), eq(10)))
                .thenReturn(reservation);

        Reservation result = repository.insert(reservation);

        assertThat(result.getUserId()).isEqualTo(2);
        assertThat(result.getEventId()).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    // --- setCancelled ---

    @Test
    void setCancelledReturnsTrueWhenRowIsUpdated() {
        when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);

        boolean result = repository.setCancelled(1);

        assertThat(result).isTrue();
    }

    @Test
    void setCancelledReturnsFalseWhenNoRowUpdated() {
        when(jdbcTemplate.update(anyString(), eq(999))).thenReturn(0);

        boolean result = repository.setCancelled(999);

        assertThat(result).isFalse();
    }

    // --- exists ---

    @Test
    @SuppressWarnings("unchecked")
    void existsReturnsTrueWhenReservationFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(2), eq(10)))
                .thenReturn(List.of(sampleReservation()));

        boolean result = repository.exists(2, 10);

        assertThat(result).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void existsReturnsFalseWhenNoReservationFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(2), eq(10))).thenReturn(List.of());

        boolean result = repository.exists(2, 10);

        assertThat(result).isFalse();
    }
}
