package no.personligfrelser.icmapi.mapper;

import no.personligfrelser.icmapi.MeasurementUtils;
import no.personligfrelser.icmapi.model.Measurement;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MeasurementMapper implements RowMapper<Measurement> {
	@Override
	public Measurement mapRow(ResultSet rs, int i) throws SQLException {
		String deviceName = rs.getString("name");
		Timestamp timestamp = rs.getTimestamp("timestamp");
		return new Measurement(rs.getInt("mm.id"), deviceName, timestamp,
				MeasurementUtils.convertResultToMap("temp", rs),
				MeasurementUtils.convertResultToMap("co2", rs),
				MeasurementUtils.convertResultToMap("hum", rs),
				MeasurementUtils.convertResultToMap("dust", rs),
				MeasurementUtils.convertResultToMap("light", rs));
	}
}
