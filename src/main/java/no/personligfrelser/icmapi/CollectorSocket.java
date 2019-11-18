package no.personligfrelser.icmapi;

import no.personligfrelser.icmapi.model.Measurement;
import no.personligfrelser.icmapi.repository.MeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class CollectorSocket {
	@Value("${collector.host}")
	private String host;
	@Value("${collector.port}")
	private int port;

	// Socket variables
	private Socket con;
	private BufferedReader input;
	private PrintWriter output;

	private MeasurementRepository repo;

	@Autowired
	public CollectorSocket(MeasurementRepository repo) {
		this.repo = repo;
	}

	@PostConstruct
	public void init() {
		try {
			con = new Socket(host, port);
			input = new BufferedReader(new InputStreamReader(con.getInputStream()));
			output = new PrintWriter(con.getOutputStream());

			// Start thread
			openAndReadSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openAndReadSocket() {
		Thread t = new Thread(() -> {
			while (true) {
				try {
					StringBuilder sb = new StringBuilder();
					String s;   // Placeholder for each iteration below

					// Read input stream and get store the whole message received
					while ((s = input.readLine()) != null) {
						sb.append(s);
					}

					if (!sb.toString().equalsIgnoreCase("null") && !sb.toString().isEmpty()) {
						System.out.println(sb.toString());

						// Convert the json message to measurement instances
						List<Measurement> measurements = MeasurementUtils.convertJsonToObject(sb.toString());
						addMeasurement(measurements);
					}

				} catch (Exception e) {
					System.err.println("Couldn't open a socket");
				}
			}
		});

		t.start();
	}

	private void addMeasurement(List<Measurement> measurements) {
		repo.insertMeasurements(measurements);
	}
}
