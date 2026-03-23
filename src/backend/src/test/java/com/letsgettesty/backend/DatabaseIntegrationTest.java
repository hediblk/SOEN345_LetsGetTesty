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
		Long users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
		assertThat(users).isEqualTo(2L);

		Long events = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Long.class);
		assertThat(events).isEqualTo(1L);

		Long reservations = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Long.class);
		assertThat(reservations).isEqualTo(2L);
	}

	@Test
	void reservationsReferenceExistingUsersAndEvents() {
		Long joined = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*) FROM reservations r
				INNER JOIN users u ON r.user_id = u.id
				INNER JOIN events e ON r.event_id = e.id
				""",
				Long.class);
		assertThat(joined).isEqualTo(2L);
	}
}
