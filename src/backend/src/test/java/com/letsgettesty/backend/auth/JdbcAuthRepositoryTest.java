package com.letsgettesty.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class JdbcAuthRepositoryTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final JdbcAuthRepository repository = new JdbcAuthRepository(jdbcTemplate);

    // --- existsByFullName ---

    @Test
    void existsByFullNameReturnsTrueWhenCountIsPositive() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("Jane"), eq("Jane"))).thenReturn(1);

        assertThat(repository.existsByFullName("Jane")).isTrue();
    }

    @Test
    void existsByFullNameReturnsFalseWhenCountIsZero() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("Jane"), eq("Jane"))).thenReturn(0);

        assertThat(repository.existsByFullName("Jane")).isFalse();
    }

    @Test
    void existsByFullNameReturnsFalseWhenNullReturned() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("Jane"), eq("Jane"))).thenReturn(null);

        assertThat(repository.existsByFullName("Jane")).isFalse();
    }

    // --- existsByEmail ---

    @Test
    void existsByEmailReturnsTrueWhenCountIsPositive() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("a@b.com"), eq("a@b.com"))).thenReturn(2);

        assertThat(repository.existsByEmail("a@b.com")).isTrue();
    }

    @Test
    void existsByEmailReturnsFalseWhenCountIsZero() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("a@b.com"), eq("a@b.com"))).thenReturn(0);

        assertThat(repository.existsByEmail("a@b.com")).isFalse();
    }

    // --- existsByPhone ---

    @Test
    void existsByPhoneReturnsTrueWhenCountIsPositive() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("514-555-0100"), eq("514-555-0100")))
                .thenReturn(1);

        assertThat(repository.existsByPhone("514-555-0100")).isTrue();
    }

    @Test
    void existsByPhoneReturnsFalseWhenCountIsZero() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("514-555-0100"), eq("514-555-0100")))
                .thenReturn(0);

        assertThat(repository.existsByPhone("514-555-0100")).isFalse();
    }

    // --- findUsersByLoginContact ---

    @Test
    void findUsersByLoginContactReturnsMatchingAccounts() {
        AccountRecord account = new AccountRecord(1, AccountRole.USER, "Jane", "secret", "jane@example.com", null);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("jane@example.com"), eq("jane@example.com")))
                .thenReturn(List.of(account));

        List<AccountRecord> result = repository.findUsersByLoginContact("jane@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("jane@example.com");
        assertThat(result.get(0).role()).isEqualTo(AccountRole.USER);
    }

    @Test
    void findUsersByLoginContactReturnsEmptyWhenNoMatch() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("unknown@example.com"), eq("unknown@example.com")))
                .thenReturn(List.of());

        List<AccountRecord> result = repository.findUsersByLoginContact("unknown@example.com");

        assertThat(result).isEmpty();
    }

    // --- findAdminsByLoginContact ---

    @Test
    void findAdminsByLoginContactReturnsMatchingAccounts() {
        AccountRecord account = new AccountRecord(2, AccountRole.ADMIN, "Admin One", "secret", null, "514-555-0100");
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("514-555-0100"), eq("514-555-0100")))
                .thenReturn(List.of(account));

        List<AccountRecord> result = repository.findAdminsByLoginContact("514-555-0100");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo(AccountRole.ADMIN);
        assertThat(result.get(0).phone()).isEqualTo("514-555-0100");
    }

    @Test
    void findAdminsByLoginContactReturnsEmptyWhenNoMatch() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("nobody@example.com"), eq("nobody@example.com")))
                .thenReturn(List.of());

        List<AccountRecord> result = repository.findAdminsByLoginContact("nobody@example.com");

        assertThat(result).isEmpty();
    }

    // --- createAccount ---

    @Test
    void createAccountReturnsCreatedUserAccount() {
        AccountRecord created = new AccountRecord(10, AccountRole.USER, "New User", "hashed", "new@example.com", null);
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class),
                eq("New User"), eq("hashed"), eq("new@example.com"), eq((String) null)))
                .thenReturn(created);

        AccountRecord result = repository.createAccount(
                AccountRole.USER, "New User", "hashed", "new@example.com", null);

        assertThat(result.id()).isEqualTo(10);
        assertThat(result.role()).isEqualTo(AccountRole.USER);
        assertThat(result.fullName()).isEqualTo("New User");
    }

    @Test
    void createAccountReturnsCreatedAdminAccount() {
        AccountRecord created = new AccountRecord(11, AccountRole.ADMIN, "Admin Two", "hashed", null, "514-000-0001");
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class),
                eq("Admin Two"), eq("hashed"), eq((String) null), eq("514-000-0001")))
                .thenReturn(created);

        AccountRecord result = repository.createAccount(
                AccountRole.ADMIN, "Admin Two", "hashed", null, "514-000-0001");

        assertThat(result.id()).isEqualTo(11);
        assertThat(result.role()).isEqualTo(AccountRole.ADMIN);
    }
}
