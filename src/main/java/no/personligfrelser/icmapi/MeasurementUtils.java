package no.personligfrelser.icmapi;

import no.personligfrelser.icmapi.model.Measurement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class MeasurementUtils {

	/**
	 * Converts result sets from SQL queries to a Map for single measurement type ex: temperature.
	 *
	 * @param tableAlias        table name or alias defined in query to get values from
	 * @param rs                the results from the query
	 * @return                  a Map from the result set
	 * @throws SQLException     when the result set extraction failed
	 */
	public static Map<String, Float> convertResultToMap(String tableAlias, ResultSet rs) throws SQLException {
		HashMap<String, Float> map = new HashMap<>();

		if (!tableAlias.isEmpty()) {
			tableAlias = tableAlias.concat("."); // Add comma separator to get from specific table
		}

		map.put("current", rs.getFloat(tableAlias.concat("current")));
		map.put("min", rs.getFloat(tableAlias.concat("min")));
		map.put("max", rs.getFloat(tableAlias.concat("max")));
		map.put("avg", rs.getFloat(tableAlias.concat("avg")));
		map.put("llm", rs.getFloat(tableAlias.concat("llm")));
		map.put("hlm", rs.getFloat(tableAlias.concat("hlm")));

		return map;
	}

	/**
	 * Converts JSON string into a list of measurement instances.
	 * @param json
	 * @return
	 * @throws ParseException
	 */
	public static List<Measurement> convertJsonToObject(String json) throws ParseException {
		AtomicReference<List<Measurement>> measurements = new AtomicReference<>();
		measurements.set(new ArrayList<Measurement>());

		JSONArray j = (JSONArray) new JSONParser().parse(json);

		j.forEach(v -> {
			try {
				JSONObject vO = (JSONObject) v;
				String deviceName = vO.get("NAME").toString();
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());

				Map<String, Float> temp     = (Map<String, Float>) vO.get(0);
				Map<String, Float> co2      = (Map<String, Float>) vO.get(3);
				Map<String, Float> hum      = (Map<String, Float>) vO.get(1);
				Map<String, Float> dust     = (Map<String, Float>) vO.get(4);
				Map<String, Float> light    = (Map<String, Float>) vO.get(2);

				Measurement m = new Measurement(deviceName, timestamp, temp, co2, hum, dust, light);
				measurements.get().add(m);

			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return measurements.get();
	}
}
