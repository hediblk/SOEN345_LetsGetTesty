package com.letsgettesty.backend.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.letsgettesty.backend.model.User;

class JdbcUserRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final JdbcUserRepository repository = new JdbcUserRepository(jdbcTemplate);

    private User sampleUser() {
        return new User(42, "Jane Doe", "hashed-password", "jane@example.com", null);
    }

    // --- findById ---

    @Test
    @SuppressWarnings("unchecked")
    void findByIdReturnsUserWhenFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(42))).thenReturn(List.of(sampleUser()));

        Optional<User> result = repository.findById(42);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(42);
        assertThat(result.get().getFullName()).isEqualTo("Jane Doe");
        assertThat(result.get().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByIdReturnsEmptyOptionalWhenNotFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(999))).thenReturn(List.of());

        Optional<User> result = repository.findById(999);

        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByIdReturnsUserWithPhoneWhenEmailIsNull() {
        User userWithPhone = new User(7, "Sam Smith", "hashed", null, "514-555-0200");
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(7))).thenReturn(List.of(userWithPhone));

        Optional<User> result = repository.findById(7);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isNull();
        assertThat(result.get().getPhone()).isEqualTo("514-555-0200");
    }
}
