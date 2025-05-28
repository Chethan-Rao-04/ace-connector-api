/*
 * Pseudonymization Connector Library
 * A client library for interacting with the pseudonymization REST service.
 * Requires dependencies: spring-web, jackson-databind, keycloak-admin-client, lombok
 */
package org.trustdeck.ace.client.service;


import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustdeck.ace.client.dto.PseudonymDto;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Client library for interacting with a REST-based pseudonymization service.
 * Handles HTTP requests for pseudonym operations and Keycloak authentication.
 */
public class PseudonymizationConnector {
    private final String serviceUrl;
    private final RestTemplate restTemplate;
    private final Keycloak keycloakClient;
    private String accessToken;

    /**
     * Initializes the connector with service and authentication configurations.
     *
     * @param serviceUrl   URI to the pseudonymization service.
     * @param keycloakUrl  URI to the Keycloak instance.
     * @param realm        Keycloak realm.
     * @param clientId     Keycloak client ID.
     * @param clientSecret Keycloak client secret.
     * @param username     Keycloak username.
     * @param password     Keycloak user password.
     */
    public PseudonymizationConnector(String serviceUrl, String keycloakUrl, String realm,
                                     String clientId, String clientSecret, String username, String password) {
        this.serviceUrl = serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/";
        this.restTemplate = new RestTemplate();
        this.keycloakClient = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .grantType("password")
                .build();
        refreshAccessToken();
    }

    /**
     * Refreshes the Keycloak access token.
     */
    private void refreshAccessToken() {
        AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();
        this.accessToken = tokenResponse.getToken();
    }

    /**
     * Creates HTTP headers with authentication and content type.
     *
     * @return Configured HttpHeaders.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        return headers;
    }

    /**
     * Converts the response to a list regardless of whether it's a single object or array
     */
    @SuppressWarnings("unchecked")
    private List<PseudonymDto> convertResponse(Object responseBody) {
        if (responseBody == null) {
            return List.of();
        } else if (responseBody instanceof List) {
            return (List<PseudonymDto>) responseBody;
        } else if (responseBody instanceof PseudonymDto) {
            return List.of((PseudonymDto) responseBody);
        } else if (responseBody instanceof PseudonymDto[]) {
            return List.of((PseudonymDto[]) responseBody);
        }
        return List.of();
    }

    /**
     * Creates a batch of pseudonym records.
     *
     * @param domainName       The domain for the pseudonyms.
     * @param omitPrefix       If true, omits the domain prefix.
     * @param pseudonymDtoList List of pseudonym data to create.
     * @return ResponseEntity with created pseudonyms and HTTP 201 Created status.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> createPseudonymBatch(String domainName, boolean omitPrefix, List<PseudonymDto> pseudonymDtoList) {
        try {
            // Construct URL with omitPrefix parameter
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonyms?omitPrefix=" + omitPrefix;
            HttpEntity<List<PseudonymDto>> request = new HttpEntity<>(pseudonymDtoList, createHeaders());
            ResponseEntity<Object> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Object.class);

            List<PseudonymDto> result = convertResponse(response.getBody());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return createPseudonymBatch(domainName, omitPrefix, pseudonymDtoList);
            }
            throw new RuntimeException("Failed to create pseudonym batch: " + e.getMessage());
        }
    }

    /**
     * Creates a single pseudonym record.
     *
     * @param domainName   The domain for the pseudonym.
     * @param pseudonymDto The pseudonym data.
     * @param omitPrefix   If true, omits the domain prefix.
     * @return ResponseEntity with created pseudonym and HTTP 201 Created status.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> createPseudonym(String domainName, PseudonymDto pseudonymDto, boolean omitPrefix) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?omitPrefix=" + omitPrefix;
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, createHeaders());
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.status(HttpStatus.CREATED).body(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return createPseudonym(domainName, pseudonymDto, omitPrefix);
            }
            throw new RuntimeException("Failed to create pseudonym: " + e.getMessage());
        }
    }

    /**
     * Retrieves linked pseudonyms between domains.
     *
     * @param sourceDomain     The source domain.
     * @param targetDomain     The target domain.
     * @param sourceIdentifier The source identifier (optional).
     * @param sourceIdType     The source idType (optional).
     * @param sourcePsn        The source pseudonym (optional).
     * @return ResponseEntity with linked pseudonyms.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> getLinkedPseudonyms(String sourceDomain, String targetDomain, String sourceIdentifier, String sourceIdType, String sourcePsn) {
        try {
            // Build URL with optional query parameters
            StringBuilder urlSB = new StringBuilder(serviceUrl + "api/pseudonymization/domains/linked-pseudonyms?");
            urlSB.append("sourceDomain=").append(sourceDomain)
                    .append("&targetDomain=").append(targetDomain);
            if (sourceIdentifier != null) urlSB.append("&sourceIdentifier=").append(sourceIdentifier);
            if (sourceIdType != null) urlSB.append("&sourceIdType=").append(sourceIdType);
            if (sourcePsn != null) urlSB.append("&sourcePsn=").append(sourcePsn);
            String url = urlSB.toString();
            HttpEntity<PseudonymDto> request = new HttpEntity<>(createHeaders());
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return getLinkedPseudonyms(sourceDomain, targetDomain, sourceIdentifier, sourceIdType, sourcePsn);
            }
            throw new RuntimeException("Failed to get linked pseudonyms: " + e.getMessage());
        }
    }

    /**
     * Retrieves a pseudonym by identifier and idType.
     *
     * @param domainName The domain name.
     * @param identifier The record identifier.
     * @param idType     The identifier type.
     * @return ResponseEntity with matching pseudonyms.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> getPseudonymByIdentifier(String domainName, String identifier, String idType) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?id=" + identifier + "&idType=" + idType;
            HttpEntity<PseudonymDto> request = new HttpEntity<>(createHeaders());
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return getPseudonymByIdentifier(domainName, identifier, idType);
            }
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage());
        }
    }

    /**
     * Retrieves a pseudonym by pseudonym value.
     *
     * @param domainName The domain name.
     * @param psn        The pseudonym value.
     * @return ResponseEntity with matching pseudonyms.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> getPseudonymByPsn(String domainName, String psn) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?psn=" + psn;
            HttpEntity<PseudonymDto> request = new HttpEntity<>(createHeaders());
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return getPseudonymByPsn(domainName, psn);
            }
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage());
        }
    }

    /**
     * Retrieves all pseudonyms in a domain.
     *
     * @param domainName The domain name.
     * @return ResponseEntity with pseudonym list.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> getPseudonymBatch(String domainName) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonyms";
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return getPseudonymBatch(domainName);
            }
            throw new RuntimeException("Failed to fetch pseudonym batch: " + e.getMessage());
        }
    }

    /**
     * Updates a batch of pseudonym records.
     *
     * @param domainName       The domain name.
     * @param pseudonymDtoList List of updated pseudonym data.
     * @return ResponseEntity indicating success.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymBatch(String domainName, List<PseudonymDto> pseudonymDtoList) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonyms";
            HttpEntity<List<PseudonymDto>> request = new HttpEntity<>(pseudonymDtoList, createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updatePseudonymBatch(domainName, pseudonymDtoList);
            }
            throw new RuntimeException("Failed to update pseudonym batch: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym by identifier and idType, replacing the entire record.
     *
     * @param domainName   The domain name.
     * @param pseudonymDto The updated pseudonym data.
     * @param identifier   The record identifier.
     * @param idType       The identifier type.
     * @return ResponseEntity with updated pseudonym.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymCompleteByIdentifier(String domainName, PseudonymDto pseudonymDto,
                                                                 String identifier, String idType) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName +
                    "/pseudonym/complete?id=" + identifier + "&idType=" + idType;
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, createHeaders());
            ResponseEntity<PseudonymDto> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, PseudonymDto.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updatePseudonymCompleteByIdentifier(domainName, pseudonymDto, identifier, idType);
            }
            throw new RuntimeException("Failed to update complete pseudonym: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym by pseudonym value, replacing the entire record.
     *
     * @param domainName   The domain name.
     * @param pseudonymDto The updated pseudonym data.
     * @param psn          The pseudonym value.
     * @return ResponseEntity with updated pseudonym.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymCompleteByPsn(String domainName, PseudonymDto pseudonymDto, String psn) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName +
                    "/pseudonym/complete?psn=" + psn;
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, createHeaders());
            ResponseEntity<PseudonymDto> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, PseudonymDto.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updatePseudonymCompleteByPsn(domainName, pseudonymDto, psn);
            }
            throw new RuntimeException("Failed to update complete pseudonym: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym by identifier and idType.
     *
     * @param domainName   The domain name.
     * @param identifier   The record identifier.
     * @param idType       The identifier type.
     * @param pseudonymDto The updated pseudonym data.
     * @return ResponseEntity with updated pseudonym.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymByIdentifier(String domainName, String identifier, String idType, PseudonymDto pseudonymDto) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?id=" + identifier + "&idType=" + idType;
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, createHeaders());
            ResponseEntity<PseudonymDto> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, PseudonymDto.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updatePseudonymByIdentifier(domainName, identifier, idType, pseudonymDto);
            }
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym by pseudonym value.
     *
     * @param domainName   The domain name.
     * @param psn          The pseudonym value.
     * @param pseudonymDto The updated pseudonym data.
     * @return ResponseEntity with updated pseudonym.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymByPsn(String domainName, String psn, PseudonymDto pseudonymDto) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?psn=" + psn;
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, createHeaders());
            ResponseEntity<PseudonymDto> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, PseudonymDto.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return updatePseudonymByPsn(domainName, psn, pseudonymDto);
            }
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage());
        }
    }

    /**
     * Deletes all pseudonyms in a domain.
     *
     * @param domainName The domain name.
     * @return ResponseEntity indicating success.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> deletePseudonymBatch(String domainName) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonyms";
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return deletePseudonymBatch(domainName);
            }
            throw new RuntimeException("Failed to delete pseudonym batch: " + e.getMessage());
        }
    }

    /**
     * Deletes a pseudonym by identifier, idType, or pseudonym value.
     *
     * @param domainName The domain name.
     * @param identifier The record identifier (optional).
     * @param idType     The identifier type (optional).
     * @param psn        The pseudonym value (optional).
     * @throws RuntimeException If the request fails.
     */
    public void deletePseudonym(String domainName, String identifier, String idType, String psn) {
        try {
            // Construct URL with appropriate query parameters
            StringBuilder url = new StringBuilder(serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym?");
            if (identifier != null && idType != null) {
                url.append("id=").append(identifier).append("&idType=").append(idType);
            } else if (psn != null) {
                url.append("psn=").append(psn);
            } else {
                throw new IllegalArgumentException("Either identifier and idType or psn must be provided");
            }
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url.toString(), HttpMethod.DELETE, request, Void.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                deletePseudonym(domainName, identifier, idType, psn);
                return;
            }
            throw new RuntimeException("Failed to delete pseudonym: " + e.getMessage());
        }
    }

    /**
     * Validates a pseudonym value.
     *
     * @param domainName The domain name.
     * @param psn        The pseudonym value.
     * @return ResponseEntity with validation result.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> validatePseudonym(String domainName, String psn) {
        try {
            String url = serviceUrl + "api/pseudonymization/domains/" + domainName + "/pseudonym/validation?psn=" + psn;
            HttpEntity<?> request = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                refreshAccessToken();
                return validatePseudonym(domainName, psn);
            }
            throw new RuntimeException("Failed to validate pseudonym: " + e.getMessage());
        }
    }


}