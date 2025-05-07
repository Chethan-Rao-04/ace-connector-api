/*
 * Pseudonymization Connector Library
 * A client library for interacting with the pseudonymization REST service.
 * Requires dependencies: spring-web, jackson-databind, keycloak-admin-client, lombok
 */
package org.trustdeck.ace.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustdeck.ace.client.config.PseudonymizationProperties;
import org.trustdeck.ace.client.dto.RecordDto;

import java.util.List;

/**
 * A connector library for programmatic interaction with a REST-based pseudonymization service.
 * Provides methods for primary pseudonymization operations (create, retrieve, update, delete)
 * and handles Keycloak authentication using Client Credentials flow.
 */
public class PseudonymizationConnector {
    private final String serviceUrl; // Base URL of the pseudonymization service
    private final RestTemplate restTemplate; // HTTP client for REST API calls
    private final Keycloak keycloakClient; // Keycloak client for authentication
    private String accessToken; // Current access token for API authentication
    private final ObjectMapper objectMapper; // JSON serializer/deserializer

    /**
     * Private constructor to initialize the connector with configuration.
     * @param props Configuration properties containing service URL and Keycloak parameters
     */
    public PseudonymizationConnector(PseudonymizationProperties props) {
        this.serviceUrl = props.getServiceUrl().endsWith("/") ? props.getServiceUrl() : props.getServiceUrl() + "/";
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();

        // Initialize Keycloak client for authentication
        this.keycloakClient = KeycloakBuilder.builder()
                .serverUrl(props.getKeycloakUrl())
                .realm(props.getRealm())
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .grantType("client_credentials")
                .build();

        // Obtain initial access token for API calls
        refreshAccessToken();
    }

    /**
     * Refreshes the access token from Keycloak using Client Credentials flow.
     */
    private void refreshAccessToken() {
        AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();
        this.accessToken = tokenResponse.getToken();
    }

    /**
     * Creates HTTP headers with the access token and content type.
     * @return Configured HttpHeaders for API requests
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        return headers;
    }

    /**
     * Creates a new pseudonym record in the specified domain.
     * @param domainName The name of the domain
     * @param recordDto The record data to create
     * @param omitPrefix Whether to omit the domain prefix in the pseudonym
     * @return List of created RecordDto objects
     */
    public List<RecordDto> createPseudonym(String domainName, RecordDto recordDto, boolean omitPrefix) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?omitPrefix=" + omitPrefix;
            HttpEntity<RecordDto> request = new HttpEntity<>(recordDto, createHeaders());
            ResponseEntity<RecordDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    RecordDto[].class
            );
            return List.of(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return createPseudonym(domainName, recordDto, omitPrefix);
            }
            throw new RuntimeException("Failed to create pseudonym: " + e.getMessage());
        }
    }

    /**
     * Retrieves a pseudonym record by identifier and idType.
     * @param domainName The name of the domain
     * @param identifier The identifier of the record
     * @param idType The type of the identifier
     * @return List of matching RecordDto objects
     */
    public List<RecordDto> getPseudonymByIdentifier(String domainName, String identifier, String idType) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?id=" + identifier + "&idType=" + idType;
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            ResponseEntity<RecordDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    RecordDto[].class
            );
            return List.of(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return getPseudonymByIdentifier(domainName, identifier, idType);
            }
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage());
        }
    }

    /**
     * Retrieves a pseudonym record by pseudonym value.
     * @param domainName The name of the domain
     * @param psn The pseudonym value
     * @return List of matching RecordDto objects
     */
    public List<RecordDto> getPseudonymByPsn(String domainName, String psn) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?psn=" + psn;
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            ResponseEntity<RecordDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    RecordDto[].class
            );
            return List.of(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return getPseudonymByPsn(domainName, psn);
            }
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym record identified by identifier and idType.
     * @param domainName The name of the domain
     * @param identifier The identifier of the record
     * @param idType The type of the identifier
     * @param recordDto The updated record data
     * @return The updated RecordDto object
     */
    public RecordDto updatePseudonymByIdentifier(String domainName, String identifier, String idType, RecordDto recordDto) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?id=" + identifier + "&idType=" + idType;
            HttpEntity<RecordDto> request = new HttpEntity<>(recordDto, createHeaders());
            ResponseEntity<RecordDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    RecordDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updatePseudonymByIdentifier(domainName, identifier, idType, recordDto);
            }
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym record identified by pseudonym value.
     * @param domainName The name of the domain
     * @param psn The pseudonym value
     * @param recordDto The updated record data
     * @return The updated RecordDto object
     */
    public RecordDto updatePseudonymByPsn(String domainName, String psn, RecordDto recordDto) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?psn=" + psn;
            HttpEntity<RecordDto> request = new HttpEntity<>(recordDto, createHeaders());
            ResponseEntity<RecordDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    RecordDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updatePseudonymByPsn(domainName, psn, recordDto);
            }
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage());
        }
    }

    /**
     * Deletes a pseudonym record identified by identifier, idType, or pseudonym.
     * @param domainName The name of the domain
     * @param identifier The identifier of the record (optional)
     * @param idType The type of the identifier (optional)
     * @param psn The pseudonym value (optional)
     */
    public void deletePseudonym(String domainName, String identifier, String idType, String psn) {
        try {
            StringBuilder url = new StringBuilder(serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?");
            if (identifier != null && idType != null) {
                url.append("id=").append(identifier).append("&idType=").append(idType);
            } else if (psn != null) {
                url.append("psn=").append(psn);
            } else {
                throw new IllegalArgumentException("Either identifier and idType or psn must be provided");
            }
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            restTemplate.exchange(
                    url.toString(),
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                deletePseudonym(domainName, identifier, idType, psn);
                return;
            }
            throw new RuntimeException("Failed to delete pseudonym: " + e.getMessage());
        }
    }
}