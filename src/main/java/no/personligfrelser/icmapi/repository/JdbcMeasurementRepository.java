package no.personligfrelser.icmapi.repository;

import no.personligfrelser.icmapi.MeasurementRepository;
import no.personligfrelser.icmapi.mapper.MeasurementMapper;
import no.personligfrelser.icmapi.model.Measurement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class JdbcMeasurementRepository implements MeasurementRepository {
	private final JdbcTemplate jdbc;
	private final MeasurementMapper mapper;

	// Measurement queries used as starting point
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
	public JdbcMeasurementRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
		this.mapper = new MeasurementMapper();
	}

	@Override
	public List<Measurement> findAllMeasurements() {
		return jdbc.query(measurement.get("ALL").toString().concat(" ORDER BY mm.id DESC LIMIT 10"), mapper);
	}

	@Override
	public List<Measurement> findAllMeasurementsByTime(long from, long to) {
		Timestamp tFrom = new Timestamp(from);
		Timestamp tTo = new Timestamp(to);

		return jdbc.query(measurement.get("ALL_BETWEEN_TIMESTAMP").toString(), new Object[]{tFrom, tTo}, mapper);
	}

	@Override
	public List<Measurement> findAllMeasurementsByDeviceNameAndTime(String device, long from, long to) {
		Timestamp tFrom = new Timestamp(from);
		Timestamp tTo = new Timestamp(to);

		return jdbc.query(measurement.get("ALL_FROM_DEVICE_AND_TIMESTAMP").toString(), new Object[]{tFrom, tTo, device},
				mapper);
	}

	@Override
	public int save(Measurement m) {
		String checkDuplicatesSql = "SELECT COUNT(id) AS c FROM measurement_meta AS mm WHERE mm.id IN (SELECT id FROM devices AS d WHERE d.name = ?) AND mm.timestamp = ?";
		String findDeviceByName = "SELECT id FROM devices WHERE name = ? LIMIT 1";
		String insertDevice = "INSERT INTO devices(`name`, `location`) VALUES(?, '')";
		String insertMeasurementMeta = "INSERT INTO measurement_meta(`timestamp`, `device_id`) VALUES(?, ?)";
		String insertMeasurement = "INSERT INTO measurements(`type`, mm_id, `current`, `min`, `max`, `avg`, llm, hlm)" +
		" VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

		// Check if measurement already has been inserted in database
		int foundDuplicates;

		try {
			foundDuplicates = jdbc.queryForObject(checkDuplicatesSql, new Object[]{m.getDeviceName(), m.getTimestamp()}, Integer.class);
		} catch (NullPointerException npe) {
			// Found duplicates keeps its initial value (0)
			foundDuplicates = 0;
			npe.printStackTrace();
		}

		if (foundDuplicates > 0) {
			return 0; // Measurement found, record not inserted
		}

		// Check if device name already exists, if it does, store it
		int deviceId = jdbc.queryForObject(findDeviceByName, new Object[]{m.getDeviceName()}, Integer.class);

		// If device id is 0 (i.e. device is not in the database), insert the device and get its identity
		if (deviceId != 0) {
			KeyHolder key = new GeneratedKeyHolder();
			jdbc.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
					PreparedStatement pstmt = con.prepareStatement(insertDevice, new String[]{"id"});
					pstmt.setString(1, m.getDeviceName());
					return pstmt;
				}
			}, key);

			deviceId = Integer.parseInt(key.getKey().toString());
		}

		AtomicInteger deviceIdAtomic = new AtomicInteger(deviceId);
		KeyHolder key = new GeneratedKeyHolder();

		jdbc.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement pstmt = con.prepareStatement(insertMeasurementMeta, new String[]{"id"});
				pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
				pstmt.setInt(2, deviceIdAtomic.get());
				return pstmt;
			}
		}, key);

		int mmId = Integer.parseInt(key.getKey().toString());

		// Turn of auto commit and insert the whole batch as single transaction

		List<Map<String, Float>> measurementTypeList = new ArrayList<Map<String, Float>>() {{
			add(m.getCo2());
			add(m.getDust());
			add(m.getHumidity());
			add(m.getLight());
			add(m.getTemp());

			// Remove all non existing measurement types
			removeAll(Collections.singletonList(null));
		}};

		List<String> keys = new ArrayList<String>() {{
			add("co2");
			add("dust");
			add("humidity");
			add("light");
			add("temperature");
		}};

		List<Object[]> params = new ArrayList<Object[]>();

		for (int i = 0; i < measurementTypeList.size(); i++) {
			Map<String, Float> v = measurementTypeList.get(i);

			params.add(new Object[] {
					keys.get(i), mmId, v.get("Current"), v.get("Min"), v.get("Max"), v.get("Average"), 2, 2
			});
		}

		jdbc.batchUpdate(insertMeasurement, params);
		return 1;
	}

	@Override
	public void deleteAll() {
		jdbc.update("DELETE FROM measurements_meta WHERE id > 2");
		jdbc.update("DELETE FROM measurements WHERE id > 6");
		jdbc.update("DELETE FROM devices WHERE id > 2");
	}
}
