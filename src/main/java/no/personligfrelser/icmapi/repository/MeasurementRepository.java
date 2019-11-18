package no.personligfrelser.icmapi.repository;

import no.personligfrelser.icmapi.Database;
import no.personligfrelser.icmapi.MeasurementUtils;
import no.personligfrelser.icmapi.model.Measurement;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("SqlDialectInspection")
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
					"ORDER BY mm.id DESC"*/;   // Adding condition under this statement casts SQL exception

	private Map measurement = new HashMap<String, String>() {{
		put("ALL", BASE_QUERY);
		put("SINGLE", BASE_QUERY + " WHERE mm.id = ? LIMIT 1");
		put("ALL_BETWEEN_TIMESTAMP", BASE_QUERY + " WHERE mm.timestamp BETWEEN ? AND ?");
		put("ALL_FROM_DEVICE_AND_TIMESTAMP", BASE_QUERY + " WHERE mm.timestamp BETWEEN ? AND ? AND d.name = ?");
	}};

	@Autowired
	public MeasurementRepository(Database db) {
		this.db = db;
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
					pstmt.setTimestamp(i + 1, (Timestamp) p);
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

	public void insertMeasurements(List<Measurement> measurements) {
		measurements.forEach(m -> {
			int deviceId = 0;
			int measurementMetaId = 0;

			try {
				// Check if device exists, then get/add device id
				PreparedStatement getDevice = db.getDb().prepareStatement("SELECT id FROM devices WHERE name = ?");
				getDevice.setString(1, m.getDeviceName());
				getDevice.setMaxRows(1);

				ResultSet rs = getDevice.executeQuery();

				// If device exist, get device id, else create new device and get its id
				if (!rs.wasNull()) {
					rs.next();
					deviceId = rs.getInt("id");
				} else {
					PreparedStatement ps2 = db.getDb().prepareStatement("INSERT INTO device(`name`) VALUES(?)");
					ps2.setString(1, m.getDeviceName());

					ResultSet rs2 = ps2.getGeneratedKeys();
					rs2.next();
					deviceId = rs2.getInt(1);
				}

				// Create new measurement meta data and get its id
				String insertMeasurementMeta = "INSERT INTO measurement_meta(`timestamp`, `device_id`) VALUES(?, ?)";
				PreparedStatement ps3 = db.getDb().prepareStatement(insertMeasurementMeta);
				ps3.setTimestamp(1, m.getTimestamp());
				ps3.setInt(2, deviceId);

				ResultSet rs3 = ps3.getGeneratedKeys();
				rs3.next();
				measurementMetaId = rs3.getInt(1);


				// Add each measurement types and link each of them to the same measurement meta data
				insertMeasurementElements(measurementMetaId, "temperature", m.getTemp());
				insertMeasurementElements(measurementMetaId, "humidity", m.getHumidity());
				insertMeasurementElements(measurementMetaId, "co2", m.getCo2());
				insertMeasurementElements(measurementMetaId, "dust", m.getDust());
				insertMeasurementElements(measurementMetaId, "light", m.getLight());

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void insertMeasurementElements(int measurementMetaId,
	                                       String type, Map<String, Float> map) throws SQLException {

		String sql = "INSERT INTO measurement(`type`, mm_id, `current`, `min`, `max`, `avg`, llm, hlm)" +
				" VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = db.getDb().prepareStatement(sql);

		ps.setString(1, type);
		ps.setInt(2, measurementMetaId);

		ps.setFloat(3, map.get("current"));
		ps.setFloat(4, map.get("min"));
		ps.setFloat(5, map.get("max"));
		ps.setFloat(6, map.get("avg"));
		ps.setFloat(7, map.get("llm"));
		ps.setFloat(8, map.get("hlm"));
	}
}
