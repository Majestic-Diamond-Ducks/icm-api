package no.personligfrelser.icmapi;

import no.personligfrelser.icmapi.repository.MeasurementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiTests {

	public MeasurementRepository repo;

	@Autowired
	public ApiTests(MeasurementRepository repo) {
		this.repo = repo;
	}

	@Test
	void contextLoads() {
	}

}
