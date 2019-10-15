package app;

import com.sun.istack.internal.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/sensor")
@Produces("application/json")
public class Service {
	@Inject
	private SensorStore sensorStore;

	@GET
	public Response getSensorValues(@QueryParam("from") long timestampFrom,
	                                @QueryParam("to") @DefaultValue("0") long timestampTo,
	                                @QueryParam("device") String device) {

		// Check what parameters are assigned at request and prevent unassigned values to create null exceptions
		long from = timestampFrom | 0;
		long to = (timestampTo != 0) ? timestampTo : System.currentTimeMillis();
		System.out.println("FROM: " + from);
		System.out.println("TO: " + to);

		List<SensorReading> sensorReadingList = sensorStore.getReadingsFrom(from, to, device);

		// Default response (error) message on anomalies
		Response.ResponseBuilder r = Response.serverError().entity("Something went wrong...");

		if (sensorReadingList != null) {
			if (sensorReadingList.size() > 0) {
				// Found results
				r = Response.ok(sensorReadingList);
			} else {
				// No results from query
				Response.status(Response.Status.NO_CONTENT).entity("No results");
			}
		}

		return r.build();
	}


	@GET @Path("/populate") @Produces("text/plain")
	public Response addDummyData() {
		sensorStore.addReadings();
		return Response.ok("added").build();
	}

	@POST @Path("/add")
	public Response addSensorReading(String json) {
		Response.ResponseBuilder r = Response.status(Response.Status.NOT_ACCEPTABLE);
		try {
			JSONObject readings = (JSONObject) new JSONParser().parse(json);

			if (readings.containsKey("N")) {
				String clientName =  (String) readings.get("N");
				double temp =        (double) readings.get("T");
				double humidity =    (double) readings.get("H");
				double co2 =         (double) readings.get("C");
				double dust =        (double) readings.get("D");
				double light =       (double) readings.get("L");
				// add "mode" parameter

				SensorReading sr = new SensorReading(clientName, temp, humidity, co2, dust, light);
				sr = sensorStore.addReading(sr);

				r = (sr != null) ? Response.ok() : Response.status(Response.Status.NOT_ACCEPTABLE);
			} else {
				r = Response.status(Response.Status.BAD_REQUEST);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return r.build();
	}

	@DELETE @Path("delete/all")
	public Response deleteAllReadings() {
		sensorStore.removeAll();
		return Response.ok().build();
	}
}
