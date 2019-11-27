package no.personligfrelser.icmapi;
import no.personligfrelser.icmapi.model.Measurement;
import no.personligfrelser.icmapi.repository.JdbcMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

/**
 * This is where all the endpoints are defined for the different functions.
 * Every response will be sent as JSON.
 *
 * For getting and publishing measurement, it uses the measurement store.
 *
 * Context: http://localhost:8080/measurements/ or {domain}/measurements/
 *
 * @see no.personligfrelser.icmapi.model.Measurement
 */
@RestController @RequestMapping("/measurements")
@CrossOrigin(origins = "*")
public class MeasurementController {

	private MeasurementRepository m2Repo;

	@Autowired
	public MeasurementController(JdbcMeasurementRepository m2Repo) {
		this.m2Repo = m2Repo;
	}

	/**
	 * Returns measurements from device and between timestamps if any of the parameters are
	 * defined, otherwise return all measurements. You can drop and mix any parameters you want.
	 *
	 * Example URI: http://localhost:8080/measurements/?device=test&from=1579283&to=15796352   (GET)
	 * Example URI: http://localhost:8080/measurements/?device=test&to=15796352                (GET)
	 *
	 * @param from              measurements FROM a specific time       (default value is 0)
	 * @param to                measurements TO a specific time         (default value is current time)
	 * @param device            name of device to get measurements from
	 *
	 * @return                  a list of measurements within constraints
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity getMeasurements(@RequestParam(required = false, defaultValue = "0") long from,
	                            @RequestParam(required = false, defaultValue = "0") long to,
	                            @RequestParam(required = false) String device) {

		List<Measurement> measurements = null;

		// Get measurements from the database
		if (device == null) {
			if (from == 0 && to == 0) {
				// If no time constraints aren't defined, return all measurements
				measurements = m2Repo.findAllMeasurements();
			} else {
				// If timestamp end range is 0 (i.e. empty), set it to the current time
				to = (to == 0) ? System.currentTimeMillis() : to;
				// Get records from all devices within a time period
				measurements = m2Repo.findAllMeasurementsByTime(from, to);
			}

		} else{
			measurements = m2Repo.findAllMeasurementsByDeviceNameAndTime(device, from, (to == 0) ? System.currentTimeMillis() : to);
		}

		return new ResponseEntity<List>(measurements, HttpStatus.OK);
	}

	/**
	 * Deletes all measurements from database.
	 */
	@DeleteMapping("/delete/all")
	public ResponseEntity deleteAll() {
		m2Repo.deleteAll();
		return ResponseEntity.ok().build();
	}
}
