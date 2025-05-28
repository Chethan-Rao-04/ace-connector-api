package org.trustdeck.ace.client.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the pseudonymization connector.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "pseudonymization")
public class PseudonymizationProperties {
    @NotBlank
    private String serviceUrl;
    @NotBlank
    private String keycloakUrl;
    @NotBlank
    private String realm;
    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
}