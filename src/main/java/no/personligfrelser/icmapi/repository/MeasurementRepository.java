package no.personligfrelser.icmapi.repository;

import no.personligfrelser.icmapi.Database;
import no.personligfrelser.icmapi.MeasurementUtils;
import no.personligfrelser.icmapi.model.Measurement;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MeasurementRepository {
	private Database db;
	// Device queries
	private Map device = new HashMap<String, String>() {{
		put("GET_ALL", "SELECT * FROM devices");
		put("GET_SINGLE", "SELECT * FROM devices WHERE id = ? LIMIT 1");
	}};

	// Measurement queries
	private static final String BASE_QUERY =
			"SELECT * FROM `measurement_meta` AS mm " +
					"JOIN devices AS d ON mm.device_id = d.id " +
					"LEFT OUTER JOIN measurements AS temp ON mm.id = temp.mm_id AND temp.type = \"temperature\" " +
					"LEFT OUTER JOIN measurements AS co2 ON mm.id = co2.mm_id AND co2.type = \"co2\" " +
					"LEFT OUTER JOIN measurements AS hum ON mm.id = hum.mm_id AND hum.type = \"humidity\" " +
					"LEFT OUTER JOIN measurements AS dust ON mm.id = dust.mm_id AND dust.type = \"dust\" " +
					"LEFT OUTER JOIN measurements AS light ON mm.id = light.mm_id AND light.type = \"light\" " /*+
					"ORDER BY mm.id DESC"*/;   // Adding condition under this element casts SQL exception

	private Map measurement = new HashMap<String, String>() {{
		put("ALL", BASE_QUERY);
		put("SINGLE", BASE_QUERY + " WHERE mm.id = ? LIMIT 1");
		put("ALL_BETWEEN_TIMESTAMP", BASE_QUERY + " WHERE mm.timestamp BETWEEN ? AND ?");
		put("ALL_FROM_DEVICE_AND_TIMESTAMP", BASE_QUERY + " WHERE mm.timestamp BETWEEN ? AND ? WHERE d.name = ?");
	}};

	public MeasurementRepository() {
		try {
			db = new Database();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Couldn't establish connection with database!");
		}
	}

	public List<Measurement> findAllByTimestampBetween(long from, long to) {
		Timestamp tFrom = new Timestamp(from);
		Timestamp tTo = new Timestamp(to);

		return findAsListFromQuery(measurement.get("ALL_BETWEEN_TIMESTAMP").toString(), tFrom, tTo);
	}

	public List<Measurement> findAllByClientNameAndTimestampBetween(String device, long from, long to) {
		Timestamp tFrom = new Timestamp(from);
		Timestamp tTo = new Timestamp(to);

		return findAsListFromQuery(measurement.get("ALL_FROM_DEVICE_AND_TIMESTAMP").toString(), tFrom, tTo, device);
	}

	public List<Measurement> findAll() {
		return findAsListFromQuery(measurement.get("ALL").toString());
	}

	private List<Measurement> findAsListFromQuery(String sql, Object ... params) {
		ArrayList<Measurement> measurements = new ArrayList<>();

		try {
			PreparedStatement pstmt = db.getDb().prepareStatement(sql);

			// Assign parameters based on what type they are
			for (int i = 0; i < params.length; i++) {
				Object p = params[i];

				if (p.getClass() == String.class) {
					pstmt.setString(i + 1, (String) p);
				} else if (p.getClass() == Integer.class) {
					pstmt.setInt(i + 1, (int) p);
				} else if (p.getClass() == Timestamp.class) {
					pstmt.setTimestamp(i + 1, new Timestamp((Long) p));
				} else if (p.getClass() == Long.class) {
					pstmt.setLong(i + 1, (long) p);
				} else {
					throw new SQLException("COULDN'T CONVERT!");
				}
			}

			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String deviceName = rs.getString("name");
				Timestamp timestamp = rs.getTimestamp("timestamp");
				measurements.add(new Measurement(rs.getInt("mm.id"), deviceName, timestamp,
										MeasurementUtils.convertResultToMap("temp", rs),
										MeasurementUtils.convertResultToMap("co2", rs),
										MeasurementUtils.convertResultToMap("hum", rs),
										MeasurementUtils.convertResultToMap("dust", rs),
										MeasurementUtils.convertResultToMap("light", rs)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return measurements;
	}
}
