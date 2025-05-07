package org.trustdeck.ace.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.trustdeck.ace.client.config.PseudonymizationProperties;

/**
 * Main Spring Boot application class for the pseudonymization connector API.
 */

@SpringBootApplication
@EnableConfigurationProperties(PseudonymizationProperties.class)
public class ApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}
}