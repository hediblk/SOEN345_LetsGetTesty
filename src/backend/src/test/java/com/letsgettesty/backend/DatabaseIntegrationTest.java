package com.letsgettesty.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class DatabaseIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void schemaAndSeedDataArePresent() {
		Long seedUsers = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users WHERE id IN (1, 2)", Long.class);
		assertThat(seedUsers).isEqualTo(2L);

		Long seedEvents = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM events WHERE id IN (1, 2, 3, 5, 6, 8)", Long.class);
		assertThat(seedEvents).isEqualTo(6L);

		Long seedReservations = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM reservations WHERE id IN (1, 2)", Long.class);
		assertThat(seedReservations).isEqualTo(2L);
	}

	@Test
	void reservationsReferenceExistingUsersAndEvents() {
		Long joined = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*) FROM reservations r
				INNER JOIN users u ON r.user_id = u.id
				INNER JOIN events e ON r.event_id = e.id
				WHERE r.id IN (1, 2)
				""",
				Long.class);
		assertThat(joined).isEqualTo(2L);
	}
}
