package app;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.bind.annotation.JsonbPropertyOrder;
import javax.persistence.*;

/**
 * This is an entity that has data from measurements. This class also holds a few of the
 * common queries that will be used by the controller.
 *
 * @see app.MeasurementStore
 */
@Data
@NoArgsConstructor
@Entity(name = "Measurements")
@JsonbPropertyOrder({"id", "clientName", "timestamp", "temperature", "humidity", "co2", "dust", "light"})
@NamedQueries({
		@NamedQuery(name = "getMeasurements", query = "SELECT sr FROM Measurements sr"),
		@NamedQuery(name = "getMeasurementsFromDevice",
				query = "SELECT sr FROM Measurements sr WHERE sr.clientName = :cn"),
		@NamedQuery(name = "getAllMeasurementsFromWithDevice",
				query = "SELECT sr FROM Measurements sr WHERE sr.clientName = :cn " +
						"AND sr.timestamp BETWEEN :tf AND :tt"),
		@NamedQuery(name = "getAllMeasurementsFrom",
				query = "SELECT sr FROM Measurements sr WHERE sr.timestamp BETWEEN :tf AND :tt")

})
public class Measurement {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private int id;                 // auto generated identifier
	private String clientName;      // name of the device
	private long timestamp;         // when the measurement was taken

	private double temperature;
	private double humidity;
	private double co2;
	private double dust;
	private double light;

	/**
	 * Constructor used to create the entity from the API requests and for test entries.
	 *
	 * @param clientName    device name
	 * @param temperature   measurement temperature
	 * @param humidity      measurement humidity
	 * @param co2           measurement co2
	 * @param dust          measurement dust
	 * @param light         measurement light
	 */
	public Measurement(String clientName, double temperature, double humidity, double co2, double dust, double light) {
		this.clientName = clientName;
		this.timestamp = System.currentTimeMillis(); // TODO Change this
		this.temperature = temperature;
		this.humidity = humidity;
		this.co2 = co2;
		this.dust = dust;
		this.light = light;
	}

	/**
	 * Set a random timestamp for when the measurement was taken.
	 *
	 * @return this instance
	 */
	public Measurement setRandomTimestamp() {
		this.timestamp = timestamp - ((long) (Math.random() * 100000));
		return this;
	}
}
