package com.letsgettesty.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.letsgettesty.backend.auth.JdbcAuthRepository;
import com.letsgettesty.backend.event.EventRepository;
import com.letsgettesty.backend.reservation.ReservationRepository;

@SpringBootTest(
		properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
class BackendApplicationTests {

	@MockBean
	private JdbcAuthRepository jdbcAuthRepository;

	@MockBean
	private EventRepository eventRepository;

	@MockBean
	private ReservationRepository reservationRepository;

	@Test
	void contextLoads() {
	}

}
