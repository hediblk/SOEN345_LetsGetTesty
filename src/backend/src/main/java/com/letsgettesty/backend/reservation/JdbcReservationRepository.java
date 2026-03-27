package com.letsgettesty.backend.reservation;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.letsgettesty.backend.model.Reservation;
import com.letsgettesty.backend.model.ReservationStatus;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final String RESERVATION_TABLE_COLUMNS = """
            id,
            user_id,
            event_id,
            status,
            created_at,
            cancelled_at
            """;

    private static final String FIND_ALL_SQL = """
            SELECT
            """
            + RESERVATION_TABLE_COLUMNS
            + """
            FROM reservations
            ORDER BY created_at ASC, id ASC
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT
            """
            + RESERVATION_TABLE_COLUMNS
            + """
            FROM reservations
            WHERE id = ?
            """;

    private static final String FIND_BY_USER_ID_SQL = """
            SELECT
            """
            + RESERVATION_TABLE_COLUMNS
            + """
            FROM reservations
            WHERE user_id = ?
            ORDER BY created_at ASC, id ASC
            """;

    private static final String FIND_BY_EVENT_ID_SQL = """
            SELECT
            """
            + RESERVATION_TABLE_COLUMNS
            + """
            FROM reservations
            WHERE event_id = ?
            ORDER BY created_at ASC, id ASC
            """;

    private static final String EXISTS_SQL = """
            SELECT
            """
            + RESERVATION_TABLE_COLUMNS
            + """
            FROM reservations
            WHERE user_id = ? AND event_id = ? AND status = 'CONFIRMED'
            ORDER BY created_at ASC, id ASC
            """;

    private static final String INSERT_SQL = """
            INSERT INTO reservations (
                user_id,
                event_id,
                status,
                created_at
            )
            VALUES (?, ?, 'CONFIRMED', NOW())
            RETURNING
            """
            + RESERVATION_TABLE_COLUMNS;

    private static final String SET_CANCELLED_SQL = """
            UPDATE reservations
            SET status = 'CANCELLED',
                cancelled_at = NOW()
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("event_id"),
            ReservationStatus.valueOf(rs.getString("status").toUpperCase(Locale.ROOT)),
            rs.getObject("created_at", java.time.LocalDateTime.class),
            rs.getObject("cancelled_at", java.time.LocalDateTime.class));

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, reservationRowMapper);
    }

    @Override
    public Optional<Reservation> findById(int id) {
        List<Reservation> rows = jdbcTemplate.query(FIND_BY_ID_SQL, reservationRowMapper, id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public List<Reservation> findByUserId(int userId) {
        return jdbcTemplate.query(FIND_BY_USER_ID_SQL, reservationRowMapper, userId);
    }

    @Override
    public List<Reservation> findByEventId(int eventId) {
        return jdbcTemplate.query(FIND_BY_EVENT_ID_SQL, reservationRowMapper, eventId);
    }

    @Override
    public Reservation insert(Reservation reservation) {
        return jdbcTemplate.queryForObject(
                INSERT_SQL,
                reservationRowMapper,
                reservation.getUserId(),
                reservation.getEventId());
    }

    @Override
    public boolean setCancelled(int id) {
        return jdbcTemplate.update(SET_CANCELLED_SQL, id) > 0;
    }

    @Override
    public boolean exists(int userId, int eventId) {
        return !jdbcTemplate.query(EXISTS_SQL, reservationRowMapper, userId, eventId).isEmpty();
    }
}