package com.letsgettesty.backend.notification;

import java.util.Locale;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.letsgettesty.backend.model.Notification;
import com.letsgettesty.backend.model.NotificationChannel;

@Repository
public class JdbcNotificationRepository implements NotificationRepository {

    private static final String NOTIFICATION_TABLE_COLUMNS = """
            id,
            reservation_id,
            channel,
            destination,
            message,
            sent_at
            """;

    private static final String INSERT_SQL = """
            INSERT INTO notifications (
                reservation_id,
                channel,
                destination,
                message,
                sent_at
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING
            """
            + NOTIFICATION_TABLE_COLUMNS;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Notification> notificationRowMapper = (resultSet, rowNum) -> new Notification(
            resultSet.getInt("id"),
            resultSet.getInt("reservation_id"),
            NotificationChannel.fromPersistenceValue(resultSet.getString("channel").toLowerCase(Locale.ROOT)),
            resultSet.getString("destination"),
            null,
            resultSet.getString("message"),
            resultSet.getObject("sent_at", java.time.LocalDateTime.class));

    public JdbcNotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Notification insert(Notification notification) {
        Notification saved = jdbcTemplate.queryForObject(
                INSERT_SQL,
                notificationRowMapper,
                notification.getReservationId(),
                notification.getChannel().getPersistenceValue(),
                notification.getDestination(),
                notification.getMessage(),
                notification.getSentAt());
        if (saved != null) {
            saved.setSubject(notification.getSubject());
        }
        return saved;
    }
}
