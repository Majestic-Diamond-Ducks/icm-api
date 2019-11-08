package no.personligfrelser.icmapi;
import no.personligfrelser.icmapi.model.Measurement;
import no.personligfrelser.icmapi.repository.MeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * This is where all the endpoints are defined for the different functions.
 * Every response will be sent as JSON.
 *
 * For getting and publishing measurement, it uses the measurement store.
 *
 * Context: http://localhost:8080/v1/sensor/ or {domain}/v1/sensor/
 *
 * @see no.personligfrelser.icmapi.model.Measurement
 */
@RestController @RequestMapping("/v1/sensors")
public class MeasurementService {

	private MeasurementRepository m2Repo;

	@Autowired
	public MeasurementService(MeasurementRepository m2Repo) {
		this.m2Repo = m2Repo;
	}

	/**
	 * Returns measurements from device and between timestamps if any of the parameters are
	 * defined, otherwise return all measurements. You can drop and mix any parameters you want.
	 *
	 * Example URI: http://localhost:8080/v1/sensor/?device=test&from=1579283&to=15796352   (GET)
	 * Example URI: http://localhost:8080/v1/sensor/?device=test&to=15796352                (GET)
	 *
	 * @param from              measurements FROM a specific time       (default value is 0)
	 * @param to                measurements TO a specific time         (default value is current time)
	 * @param device            name of device to get measurements from
	 *
	 * @return                  a list of measurements within constraints
	 */
	@RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity getV2(@RequestParam(required = false, defaultValue = "0") long from,
	                            @RequestParam(required = false, defaultValue = "0") long to,
	                            @RequestParam(required = false) String device) {
		// If timestamp end range is 0 (i.e. empty), set it to the current time
		to = (to == 0) ? System.currentTimeMillis() : to;
		System.out.println(from + " : " + to);

		List<Measurement> measurements = null;

		// Get measurements from the database
		if (device == null) {
			// Get records from all devices within a time period
			measurements = m2Repo.findAllByTimestampBetween(from, to);
		} else{
			measurements = m2Repo.findAllByClientNameAndTimestampBetween(device, from, to);
		}

		return new ResponseEntity<List>(measurements, HttpStatus.OK);
	}

	/**
	 * Deletes all measurements from database.
	 */
	@DeleteMapping("/delete/all")
	public ResponseEntity deleteAll() {
		//mRepo.deleteAll();
		return ResponseEntity.ok().build();
	}
}
