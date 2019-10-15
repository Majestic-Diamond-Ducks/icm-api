package app;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/sensor")
public class Config extends ResourceConfig {
	public Config() {
		packages(true, "app")
				.property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
	}
}
