package no.personligfrelser.icmapi;

import no.personligfrelser.icmapi.repository.MeasurementRepositoryDeprecated;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiTests {

	public MeasurementRepositoryDeprecated repo;

	@Autowired
	public ApiTests(MeasurementRepositoryDeprecated repo) {
		this.repo = repo;
	}

	@Test
	void contextLoads() {
	}

}
