package com.letsgettesty.backend.user;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.letsgettesty.backend.model.User;

@Repository
public class JdbcUserRepository implements UserRepository {

    private static final String FIND_BY_ID_SQL = """
            SELECT id, full_name, password_hash, email, phone
            FROM users
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (resultSet, rowNum) -> new User(
            resultSet.getInt("id"),
            resultSet.getString("full_name"),
            resultSet.getString("password_hash"),
            resultSet.getString("email"),
            resultSet.getString("phone"));

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findById(int id) {
        List<User> rows = jdbcTemplate.query(FIND_BY_ID_SQL, userRowMapper, id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}
