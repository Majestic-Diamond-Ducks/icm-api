package no.personligfrelser.icmapi.repository;

import no.personligfrelser.icmapi.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MeasurementRepository extends JpaRepository<Measurement, Integer> {
	List<Measurement> findAllByClientName(String name);
	List<Measurement> findAllByClientNameAndTimestampBetween(String name, long from, long to);
	List<Measurement> findAllByTimestampBetween(long from, long to);
}
