package no.personligfrelser.icmapi;
import no.personligfrelser.icmapi.model.Measurement;
import no.personligfrelser.icmapi.repository.MeasurementRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	private MeasurementRepository mRepo;

	@Autowired
	public MeasurementService(MeasurementRepository mRepo) {
		this.mRepo = mRepo;
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
	@GetMapping
	public List<Measurement> getMeasurements(@RequestParam(required = false, defaultValue = "0") long from,
	                                         @RequestParam(required = false, defaultValue = "0") long to,
	                                         @RequestParam(required = false) String device) {

		// If timestamp end range is 0 (i.e. empty), set it to the current time
		to = (to == 0) ? System.currentTimeMillis() : to;

		List<Measurement> measurements = null;

		// Get measurements from the database
		if (device == null) {
			// Get records from all devices within a time period
			measurements = mRepo.findAllByTimestampBetween(from, to);
		} else{
			measurements = mRepo.findAllByClientNameAndTimestampBetween(device, from, to);
		}

		return measurements;
	}

	/**
	 * Add single (partially or fully defined) measurement to database.
	 *
	 * @param json  measurement to commit to database provided as json
	 * @return      the same measurement, but with variables such as 'id' added
	 */
	@PostMapping
	public Measurement create(@RequestBody String json) {
		// Parse the entire json object and get the measurements
		JSONObject measurements = null;
		Measurement m = null;
		try {
			measurements = (JSONObject) new JSONParser().parse(json);

			if (measurements.containsKey("N")) {
				// If key 'N' is defined, we assume the json is formatted correctly
				String clientName =  (String) measurements.get("N");
				float temp =        (float) measurements.get("T");
				float humidity =    (float) measurements.get("H");
				float co2 =         (float) measurements.get("C");
				float dust =        (float) measurements.get("D");
				float light =       (float) measurements.get("L");
				// TODO add "mode" parameter

				// Create the measurement entity and add it to the database
				m = new Measurement(clientName, temp, humidity, co2, dust, light);
				m = mRepo.saveAndFlush(m);

				// If the entity is created (not null), the default response will change into '200 OK'
				//r = (sr != null) ? Response.ok() : Response.status(Response.Status.NOT_ACCEPTABLE);
			} else {
				// JSON object does not contain key 'N', therefore not formatted correctly
				// Set response to '400 BAD REQUEST'
				//r = Response.status(Response.Status.BAD_REQUEST).entity("Malformed request body");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return m;
	}

	/**
	 * Deletes all measurements from database.
	 */
	@DeleteMapping("/delete/all")
	public void deleteAll() {
		mRepo.deleteAll();
	}

	/**
	 * Populates the database with test measurements.
	 * @return   a list of measurements after they have been added to database
	 */
	@GetMapping("/populate")
	public List<Measurement> addDummyData() {
		// Define measurements
		Measurement s1 = new Measurement("test", 0, 2, 3,1,0)
				.setRandomTimestamp();
		Measurement s2 = new Measurement("test2", 10, 27, 473,162,99)
				.setRandomTimestamp();
		Measurement s3 = new Measurement("test3", 150, 277, 438,125,99)
				.setRandomTimestamp();
		Measurement s4 = new Measurement("test", 1980, 287, 43,1,99)
				.setRandomTimestamp();

		// Add measurements to database
		List<Measurement> measurements = new ArrayList<>(Arrays.asList(s1, s2, s3, s4));
		mRepo.saveAll(measurements);
		mRepo.flush();

		return measurements;
	}

}
