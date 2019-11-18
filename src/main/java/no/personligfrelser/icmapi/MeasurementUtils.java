package no.personligfrelser.icmapi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
			tableAlias = tableAlias.concat("."); // Add comma separator to get the table name
		}

		map.put("current", rs.getFloat(tableAlias.concat("current")));
		map.put("min", rs.getFloat(tableAlias.concat("min")));
		map.put("max", rs.getFloat(tableAlias.concat("max")));
		map.put("avg", rs.getFloat(tableAlias.concat("avg")));
		map.put("llm", rs.getFloat(tableAlias.concat("llm")));
		map.put("hlm", rs.getFloat(tableAlias.concat("hlm")));

		return map;
	}
}
