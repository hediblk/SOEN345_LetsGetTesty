package com.letsgettesty.backend.event;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.EventCategory;


@Repository
public class JdbcEventRepository implements EventRepository {

    private static final String EVENT_TABLE_COLUMNS = """
            id,
            title,
            description,
            category,
            location,
            starts_at,
            ends_at,
            capacity,
            price,
            reserved_count,
            is_cancelled
            """;

    private static final String FIND_ALL_SQL = """
            SELECT
            """
            + EVENT_TABLE_COLUMNS
            + """
            FROM events
            ORDER BY starts_at ASC, id ASC
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT
            """
            + EVENT_TABLE_COLUMNS
            + """
            FROM events
            WHERE id = ?
            """;

    private static final String INSERT_SQL = """
            INSERT INTO events (
                title,
                description,
                category,
                location,
                starts_at,
                ends_at,
                capacity,
                price,
                reserved_count,
                is_cancelled
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING
            """
            + EVENT_TABLE_COLUMNS;

    private static final String UPDATE_SQL = """
            UPDATE events
            SET title = ?,
                description = ?,
                category = ?,
                location = ?,
                starts_at = ?,
                ends_at = ?,
                capacity = ?,
                price = ?,
                reserved_count = ?
            WHERE id = ?
            """;

    private static final String SET_CANCELLED_SQL = """
            UPDATE events
            SET is_cancelled = ?
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Event> eventRowMapper = (resultSet, rowNum) -> new Event(
            resultSet.getInt("id"),
            resultSet.getString("title"),
            resultSet.getString("description"),
            parseCategory(resultSet.getString("category")),
            resultSet.getString("location"),
            resultSet.getObject("starts_at", java.time.LocalDateTime.class),
            resultSet.getObject("ends_at", java.time.LocalDateTime.class),
            resultSet.getInt("capacity"),
            resultSet.getInt("price"),
            resultSet.getInt("reserved_count"),
            resultSet.getBoolean("is_cancelled"));

    public JdbcEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Event> findAllOrderedByStartsAt() {
        return jdbcTemplate.query(FIND_ALL_SQL, eventRowMapper);
    }

    @Override
    public Optional<Event> findById(int id) {
        List<Event> rows = jdbcTemplate.query(FIND_BY_ID_SQL, eventRowMapper, id);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    @Override
    public Event insert(Event event) {
        return jdbcTemplate.queryForObject(
                INSERT_SQL,
                eventRowMapper,
                event.getTitle(),
                event.getDescription(),
                event.getCategory().name(),
                event.getLocation(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getCapacity(),
                event.getPrice(),
                event.getReservedCount(),
                event.isCancelled());
    }

    @Override
    public boolean update(Event event) {
        int updated = jdbcTemplate.update(
                UPDATE_SQL,
                event.getTitle(),
                event.getDescription(),
                event.getCategory().name(),
                event.getLocation(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getCapacity(),
                event.getPrice(),
                event.getReservedCount(),
                event.getId());
        return updated > 0;
    }

    @Override
    public boolean setCancelled(int id, boolean cancelled) {
        return jdbcTemplate.update(SET_CANCELLED_SQL, cancelled, id) > 0;
    }

    private static EventCategory parseCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Event category is missing in the database row.");
        }
        return EventCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
