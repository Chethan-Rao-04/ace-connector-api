package org.trustdeck.ace.client.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustdeck.ace.client.service.PseudonymizationConnector;

/**
 * Configuration class for pseudonymization connector beans.
 */
@Configuration
public class PseudonymizationConfig {

    @Bean
    public PseudonymizationConnector pseudonymizationConnector(PseudonymizationProperties props) {
        return new PseudonymizationConnector(props);
    }
}