package com.letsgettesty.backend.auth;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAuthRepository implements AuthRepository {

    private static final String FULL_NAME_EXISTS_SQL = """
            SELECT COUNT(*)
            FROM (
                SELECT 1 FROM users WHERE LOWER(full_name) = LOWER(?)
                UNION ALL
                SELECT 1 FROM admins WHERE LOWER(full_name) = LOWER(?)
            ) accounts
            """;

    private static final String EMAIL_EXISTS_SQL = """
            SELECT COUNT(*)
            FROM (
                SELECT 1 FROM users WHERE email IS NOT NULL AND LOWER(email) = LOWER(?)
                UNION ALL
                SELECT 1 FROM admins WHERE email IS NOT NULL AND LOWER(email) = LOWER(?)
            ) accounts
            """;

    private static final String PHONE_EXISTS_SQL = """
            SELECT COUNT(*)
            FROM (
                SELECT 1 FROM users WHERE phone = ?
                UNION ALL
                SELECT 1 FROM admins WHERE phone = ?
            ) accounts
            """;

    private static final String FIND_BY_CONTACT_SQL = """
            SELECT id, 'USER' AS role, full_name, password_hash AS password, email, phone
            FROM users
            WHERE (email IS NOT NULL AND LOWER(email) = LOWER(?)) OR phone = ?
            UNION ALL
            SELECT id, 'ADMIN' AS role, full_name, password_hash AS password, email, phone
            FROM admins
            WHERE (email IS NOT NULL AND LOWER(email) = LOWER(?)) OR phone = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<AccountRecord> accountRowMapper = (resultSet, rowNum) -> new AccountRecord(
            resultSet.getInt("id"),
            AccountRole.valueOf(resultSet.getString("role")),
            resultSet.getString("full_name"),
            resultSet.getString("password"),
            resultSet.getString("email"),
            resultSet.getString("phone"));

    public JdbcAuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByFullName(String fullName) {
        Integer count = jdbcTemplate.queryForObject(FULL_NAME_EXISTS_SQL, Integer.class, fullName, fullName);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(EMAIL_EXISTS_SQL, Integer.class, email, email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        Integer count = jdbcTemplate.queryForObject(PHONE_EXISTS_SQL, Integer.class, phone, phone);
        return count != null && count > 0;
    }

    @Override
    public AccountRecord createAccount(AccountRole role, String fullName, String password, String email, String phone) {
        String table = role == AccountRole.ADMIN ? "admins" : "users";
        String sql = "INSERT INTO " + table
                + " (full_name, password_hash, email, phone) VALUES (?, ?, ?, ?) "
                + "RETURNING id, full_name, password_hash, email, phone";

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, rowNum) -> new AccountRecord(
                        resultSet.getInt("id"),
                        role,
                        resultSet.getString("full_name"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("email"),
                        resultSet.getString("phone")),
                fullName,
                password,
                email,
                phone);
    }

    @Override
    public List<AccountRecord> findByLoginContact(String contact) {
        return jdbcTemplate.query(FIND_BY_CONTACT_SQL, accountRowMapper, contact, contact, contact, contact);
    }
}
