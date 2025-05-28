package org.trustdeck.ace.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.trustdeck.ace.client.dto.DomainDto;
import java.util.List;

/**
 * A connector library for programmatic interaction with the domain management endpoints
 * of the ACE pseudonymization service.
 * Provides methods for domain operations (create, retrieve, update, delete)
 * and handles Keycloak authentication using the password grant type.
 */
@Slf4j
public class DomainConnector {

    private final String serviceUrl; // Base URL of the pseudonymization service
    private final RestTemplate restTemplate; // HTTP client for REST API calls
    private final Keycloak keycloakClient; // Keycloak client for authentication
    private String accessToken; // Current access token for API authentication
//    private final ObjectMapper objectMapper; // JSON serializer/deserializer

    /**
     * Constructor to initialize the connector with user-provided configuration.
     *
     * @param serviceUrl   URI to the ACE instance
     * @param keycloakUrl  URI to the Keycloak instance
     * @param realm        Keycloak realm name
     * @param clientId     Keycloak client ID
     * @param clientSecret Keycloak client secret
     * @param username     Keycloak username
     * @param password     Keycloak user password
     */
    public DomainConnector(String serviceUrl, String keycloakUrl, String realm,
                           String clientId, String clientSecret, String username, String password) {
        // Ensure serviceUrl ends with a slash for consistent URL building
        this.serviceUrl = serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/";
        this.restTemplate = new RestTemplate();
//        this.objectMapper = new ObjectMapper();

        // Initialize Keycloak client for authentication
        this.keycloakClient = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .grantType("password")
                .build();

        // Obtain initial access token for API calls
        refreshAccessToken();
    }
//check before if the token is valid instead of refreshing later after the 401 error
    /**
     * Refreshes the access token from Keycloak using the password grant type.
     */
    private void refreshAccessToken() {
        AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();
        this.accessToken = tokenResponse.getToken();
    }

    /**
     * Creates HTTP headers with the access token and content type.
     *
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
     * Gets a list of all domains.
     *
     * @return List of DomainDto objects
     */
    public List<DomainDto> getAllDomains() {
        try {
            String url = serviceUrl + "api/pseudonymization/experimental/domains/hierarchy";
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            ResponseEntity<DomainDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    DomainDto[].class
            );
            return (response.getBody() != null? List.of(response.getBody()) : List.of() );
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return getAllDomains();
            }
            throw new RuntimeException("Failed to retrieve domains: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a domain by name.
     *
     * @param domainName The name of the domain
     * @return The requested DomainDto
     */
    public DomainDto getDomain(String domainName) {
        try {
            String url = serviceUrl + "api/pseudonymization/domain?name=" + domainName;
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return getDomain(domainName);
            }
            throw new RuntimeException("Failed to retrieve domain: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a specific attribute of a domain.
     *
     * @param domainName    The name of the domain
     * @param attributeName The name of the attribute to retrieve
     * @return The requested DomainDto containing only the specified attribute
     */
    public DomainDto getDomainAttribute(String domainName, String attributeName) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/" + attributeName;
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    DomainDto.class
            );        // Get status code and reason from the response
            // Then in your method:
            log.info("Response status : {} body : {}", response.getStatusCode().value(),
                    HttpStatus.valueOf(response.getStatusCode().value()).getReasonPhrase());



            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return getDomainAttribute(domainName, attributeName);
            }
            throw new RuntimeException("Failed to retrieve domain attribute: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new domain with a reduced set of attributes.
     *
     * @param domainDto The domain to create
     * @return The created DomainDto
     */
    public DomainDto createDomain(DomainDto domainDto) {
        try {
            String url = serviceUrl + "api/pseudonymization/domain";
            HttpEntity<DomainDto> request = new HttpEntity<>(domainDto, createHeaders());
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return createDomain(domainDto);
            }
            throw new RuntimeException("Failed to create domain: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new domain with all attributes.
     *
     * @param domainDto The domain to create
     * @return The created DomainDto
     */
    public DomainDto createDomainComplete(DomainDto domainDto) {
        try {
            String url = serviceUrl + "api/pseudonymization/domain/complete";
            HttpEntity<DomainDto> request = new HttpEntity<>(domainDto, createHeaders());
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return createDomainComplete(domainDto);
            }
            throw new RuntimeException("Failed to create domain: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing domain with a reduced set of attributes.
     *
     * @param domainName The name of the domain to update
     * @param domainDto  The updated domain data
     * @return The updated DomainDto
     */
    public DomainDto updateDomain(String domainName, DomainDto domainDto) {
        try {
            String url = serviceUrl + "api/pseudonymization/domain?name=" + domainName;
            HttpEntity<DomainDto> request = new HttpEntity<>(domainDto, createHeaders());
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updateDomain(domainName, domainDto);
            }
            throw new RuntimeException("Failed to update domain: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing domain with all attributes.
     *
     * @param domainName           The name of the domain to update
     * @param domainDto            The updated domain data
     * @param recursive            Whether to apply changes recursively to sub-domains
     * @return The updated DomainDto
     */
    public DomainDto updateDomainComplete(String domainName, DomainDto domainDto, boolean recursive) {
        try {
            String url = serviceUrl + "api/pseudonymization/domain/complete?name=" + domainName + "&recursive=" + recursive;
            HttpEntity<DomainDto> request = new HttpEntity<>(domainDto, createHeaders());
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updateDomainComplete(domainName, domainDto, recursive);
            }
            throw new RuntimeException("Failed to update domain: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a domain.
     *
     * @param domainName The name of the domain to delete
     * @param recursive  Whether to delete sub domains recursively
     */
    public void deleteDomain(String domainName, boolean recursive) {
        try {
            String url = serviceUrl + "api/pseudonymization/domain?name=" + domainName + "&recursive=" + recursive;
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                deleteDomain(domainName, recursive);
                return;
            }
            throw new RuntimeException("Failed to delete domain: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the salt of a domain.
     *
     * @param domainName The name of the domain
     * @param newSalt    The new salt value
     * @param allowEmpty Whether to allow an empty salt
     * @return The updated DomainDto
     */
    public DomainDto updateSalt(String domainName, String newSalt, boolean allowEmpty) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/salt?salt=" + newSalt + "&allowEmpty=" + allowEmpty;
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updateSalt(domainName, newSalt, allowEmpty);
            }
            throw new RuntimeException("Failed to update salt: " + e.getMessage(), e);
        }
    }


}