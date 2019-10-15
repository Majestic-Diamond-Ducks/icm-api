package app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity(name = "SensorReadings")
@JsonbPropertyOrder({"id", "clientName", "timestamp", "temperature", "humidity", "co2", "dust", "light"})
@NamedQueries({
		@NamedQuery(name = "getAllReadings", query = "SELECT sr FROM SensorReadings sr"),
		@NamedQuery(name = "getReadingsFromSensor",
				query = "SELECT sr FROM SensorReadings sr WHERE sr.clientName = :cn"),
		@NamedQuery(name = "getAllReadingsFromWithDevice",
				query = "SELECT sr FROM SensorReadings sr WHERE sr.clientName = :cn " +
						"AND sr.timestamp BETWEEN :tf AND :tt"),
		@NamedQuery(name = "getAllReadingsFrom",
				query = "SELECT sr FROM SensorReadings sr WHERE sr.timestamp BETWEEN :tf AND :tt")

})
public class SensorReading {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String clientName;
	private long timestamp;

	private double temperature;
	private double humidity;
	private double co2;
	private double dust;
	private double light;

	public SensorReading(String clientName, double temperature, double humidity, double co2, double dust, double light) {
		this.clientName = clientName;
		this.timestamp = System.currentTimeMillis();
		this.temperature = temperature;
		this.humidity = humidity;
		this.co2 = co2;
		this.dust = dust;
		this.light = light;
	}

	public SensorReading setRandomTimestamp() {
		this.timestamp = timestamp - ((long) (Math.random() * 100000));
		return this;
	}
}
