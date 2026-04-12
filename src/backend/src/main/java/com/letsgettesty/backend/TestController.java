package com.letsgettesty.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Connection;

@RestController
@RequestMapping("/api/test")
@Profile("dev")
public class TestController {
	private final JdbcTemplate jdbcTemplate;

    public TestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	@GetMapping("/")
	public String test() {
		return "backend running";
	}

	@PostMapping("/reset-db")
    public ResponseEntity<String> resetDb() throws Exception {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("database/reset.sql"));
        }
        return ResponseEntity.ok("Database reset");
    }
}
