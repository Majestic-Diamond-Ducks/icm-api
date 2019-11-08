package no.personligfrelser.icmapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Measurement {    // Measurement Meta
	private int id;
	private String deviceName;
	private Timestamp timestamp;

	private Map<String, Float> temp;
	private Map<String, Float> co2;
	private Map<String, Float> humidity;
	private Map<String, Float> dust;
	private Map<String, Float> light;

	public Measurement(String deviceName, Timestamp timestamp, Map<String, Float> temp,
	                   Map<String, Float> co2, Map<String, Float> humidity,
	                   Map<String, Float> dust, Map<String, Float> light) {

		this.deviceName = deviceName;
		this.timestamp = timestamp;

		this.temp = temp;
		this.co2 = co2;
		this.humidity = humidity;
		this.dust = dust;
		this.light = light;
	}
}
