package app;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * This is where all the endpoints are defined for the different functions.
 * Every response will be sent as JSON.
 *
 * For getting and publishing measurement, it uses the measurement store.
 *
 * Context: http://localhost:8080/v1/sensor/ or {domain}/v1/sensor/
 *
 * @see app.MeasurementStore
 */
@Path("/sensor") @Produces("application/json")
public class Service {
	@Inject
	private MeasurementStore measurementStore;

	/**
	 * Returns measurements from device and between timestamps if any of the parameters are
	 * defined, otherwise return all measurements. You can drop and mix any parameters you want.
	 *
	 * Example URI: http://localhost:8080/v1/sensor/?device=test&from=1579283&to=15796352   (GET)
	 * Example URI: http://localhost:8080/v1/sensor/?device=test&to=15796352                (GET)
	 *
	 * @param timestampFrom     measurements FROM a specific time
	 * @param timestampTo       measurements TO a specific time
	 * @param device            name of device to get measurements from
	 *
	 * @return                  a list of measurements within constraints
	 */
	@GET
	public Response getMeasurements(@QueryParam("from") long timestampFrom,
	                                @QueryParam("to") @DefaultValue("0") long timestampTo,
	                                @QueryParam("device") String device) {

		// Get measurements from the database
		List<Measurement> measurementList = measurementStore.getMeasurementsFrom(timestampFrom, timestampTo, device);

		// Default response (error) message on anomalies
		Response.ResponseBuilder r = Response.serverError().entity("Something went wrong...");

		if (measurementList != null) {
			if (measurementList.size() > 0) {
				// Found results
				r = Response.ok(measurementList);
			} else {
				// No results from query
				Response.status(Response.Status.NO_CONTENT).entity("No results");
			}
		}

		return r.build();
	}

	/**
	 * Populates the database with test measurements.
	 *
	 * @see     app.MeasurementStore#addDummyMeasurements()
	 * @return  response 200 OK
	 */
	@GET @Path("/populate")
	public Response addMeasurements() {
		measurementStore.addDummyMeasurements();
		return Response.ok().build();
	}

	/**
	 * Add a new measurement from a sensor device.
	 *
	 * Accepted format: https://raw.githubusercontent.com/Majestic-Diamond-Ducks/Simple-Json-Client/master/document.json
	 *
	 * @param json  measurement in json format
	 * @return      response 200 OK, if measurement was added, otherwise 406 or 400
	 */
	@POST @Path("/add") @Consumes("application/json")
	public Response addMeasurements(String json) {
		// Default response if nothing happened (HTTP 406 NOT ACCEPTED)
		Response.ResponseBuilder r = Response.status(Response.Status.NOT_ACCEPTABLE);

		try {
			// Parse the entire json object and get the measurements
			JSONObject measurements = (JSONObject) new JSONParser().parse(json);

			if (measurements.containsKey("N")) {
				// If key 'N' is defined, we assume the json is formatted correctly
				String clientName =  (String) measurements.get("N");
				double temp =        (double) measurements.get("T");
				double humidity =    (double) measurements.get("H");
				double co2 =         (double) measurements.get("C");
				double dust =        (double) measurements.get("D");
				double light =       (double) measurements.get("L");
				// TODO add "mode" parameter

				// Create the measurement entity and add it to the database
				Measurement sr = new Measurement(clientName, temp, humidity, co2, dust, light);
				sr = measurementStore.addMeasurement(sr);

				// If the entity is created (not null), the default response will change into '200 OK'
				r = (sr != null) ? Response.ok() : Response.status(Response.Status.NOT_ACCEPTABLE);
			} else {
				// JSON object does not contain key 'N', therefore not formatted correctly
				// Set response to '400 BAD REQUEST'
				r = Response.status(Response.Status.BAD_REQUEST).entity("Malformed request body");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Build the response from the response builder and send the response
		return r.build();
	}

	/**
	 * Deletes all measurements from database.
	 * @return  response 200 OK
	 */
	@DELETE @Path("delete/all")
	public Response deleteAllMeasurements() {
		measurementStore.removeAll();
		return Response.ok().build();
	}
}
