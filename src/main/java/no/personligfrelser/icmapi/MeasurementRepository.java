package no.personligfrelser.icmapi;

import no.personligfrelser.icmapi.model.Measurement;

import java.util.List;

public interface MeasurementRepository {
	List<Measurement> findAllMeasurements();
	List<Measurement> findAllMeasurementsByTime(long from, long to);
	List<Measurement> findAllMeasurementsByDeviceNameAndTime(String device, long from, long to);
	int save(Measurement m);
	void deleteAll();
}
