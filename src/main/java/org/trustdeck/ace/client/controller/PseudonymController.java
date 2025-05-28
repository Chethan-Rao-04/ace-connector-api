package org.trustdeck.ace.client.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.trustdeck.ace.client.dto.PseudonymDto;
import org.trustdeck.ace.client.service.PseudonymizationConnector;

import java.util.List;

/**
 * REST controller for managing pseudonymization operations in the ACE system.
 * Provides endpoints for creating, retrieving, updating, and deleting pseudonym records,
 * as well as searching linked pseudonyms across domains. All operations are validated
 * and authenticated via Keycloak. This controller interacts with the pseudonymization
 * service through the {@link PseudonymizationConnector}.
 *
 * @see PseudonymizationConnector
 * @see PseudonymDto
 */
@RestController
@RequestMapping("/api/pseudonymization")
@Validated
public class PseudonymController {
    private final PseudonymizationConnector connector;

    /**
     * Constructs a new PseudonymController with the specified connector.
     *
     * @param connector The service connector for interacting with the pseudonymization service.
     */
    public PseudonymController(PseudonymizationConnector connector) {
        this.connector = connector;
    }

    /**
     * Creates a batch of pseudonym records in the specified domain.
     *
     * @param domainName       The name of the domain where pseudonyms will be stored (required, non-blank).
     * @param omitPrefix       If true, omits the domain prefix from generated pseudonyms (optional, defaults to false).
     * @param pseudonymDtoList List of pseudonym data to create (required, validated).
     * @return A ResponseEntity containing a list of created {@link PseudonymDto} objects.
     *         - 200 OK: Batch created successfully.
     *         - 400 Bad Request: Invalid input data or missing required fields.
     *         - 401 Unauthorized: Authentication failure.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @PostMapping("/domains/{domain}/pseudonyms")
    public ResponseEntity<?> createPseudonymBatch(
            @PathVariable("domain") String domainName,
            @RequestParam(name="omitPrefix", required = false, defaultValue = "false") boolean omitPrefix,
            @RequestBody List<PseudonymDto> pseudonymDtoList){
        return connector.createPseudonymBatch(domainName, omitPrefix, pseudonymDtoList);
    }

    /**
     * Creates a new pseudonym record in the specified domain.
     *
     * @param domainName   The name of the domain where the pseudonym will be stored (required, non-blank).
     * @param pseudonymDto The pseudonym data to create (required, validated).
     * @param omitPrefix   If true, omits the domain prefix from the generated pseudonym (optional, defaults to false).
     * @return A ResponseEntity containing a list of created {@link PseudonymDto} objects.
     *         - 200 OK: Pseudonym created successfully.
     *         - 400 Bad Request: Invalid input data or missing required fields.
     *         - 401 Unauthorized: Authentication failure.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @PostMapping("/domains/{domain}/pseudonym")
    public ResponseEntity<?> createPseudonym(
            @PathVariable("domain") @NotBlank String domainName,
            @Valid @RequestBody PseudonymDto pseudonymDto,
            @RequestParam(defaultValue = "false") boolean omitPrefix) {
        return connector.createPseudonym(domainName, pseudonymDto, omitPrefix);
    }

    /**
     * Retrieves all pseudonym records stored in the specified domain.
     *
     * @param domainName The name of the domain from which to retrieve pseudonyms (required, non-blank).
     * @return A ResponseEntity containing a list of {@link PseudonymDto} objects.
     *         - 200 OK: List of pseudonym records returned successfully.
     *         - 400 Bad Request: Invalid domain name.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No pseudonyms found in the specified domain.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @GetMapping("/domains/{domain}/pseudonyms")
    public ResponseEntity<?> getPseudonymBatch(
            @PathVariable("domain") String domainName){
        return connector.getPseudonymBatch(domainName);
    }

    /**
     * Searches and retrieves linked pseudonyms between source and target domains.
     * The search follows the pseudonym chain in the domain tree, starting from the source domain
     * and optionally using a source identifier, idType, or pseudonym.
     *
     * @param sourceDomain     The starting domain for the search (required).
     * @param targetDomain     The target domain for the search (required).
     * @param sourceIdentifier The identifier of the record to start the search from (optional).
     * @param sourceIdType     The idType of the record to start the search from (optional).
     * @param sourcePsn        The pseudonym of the record to start the search from (optional).
     * @return A ResponseEntity containing a list of linked {@link PseudonymDto} objects.
     *         - 200 OK: Linked pseudonyms retrieved successfully.
     *         - 400 Bad Request: Invalid or missing required parameters.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No linked pseudonyms found.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @GetMapping(value = "/domains/linked-pseudonyms", params={"sourceDomain", "targetDomain"})
    public ResponseEntity<?> getLinkedPseudonyms(
            @RequestParam(name="sourceDomain", required=true) String sourceDomain,
            @RequestParam(name="targetDomain", required=true) String targetDomain,
            @RequestParam(name="sourceIdentifier", required=false) String sourceIdentifier,
            @RequestParam(name="sourceIdType", required=false) String sourceIdType,
            @RequestParam(name="sourcePsn", required=false) String sourcePsn) {
        return connector.getLinkedPseudonyms(sourceDomain, targetDomain, sourceIdentifier, sourceIdType, sourcePsn);
    }

    /**
     * Retrieves a pseudonym record by its identifier and idType.
     *
     * @param domainName The name of the domain where the pseudonym is stored (required, non-blank).
     * @param identifier The identifier of the record (required, non-blank).
     * @param idType     The type of the identifier (required, non-blank).
     * @return A ResponseEntity containing a list of matching {@link PseudonymDto} objects.
     *         - 200 OK: Pseudonym record retrieved successfully.
     *         - 400 Bad Request: Invalid or missing required parameters.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No pseudonym found for the given identifier and idType.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @GetMapping("/domains/{domain}/pseudonym/by-id")
    public ResponseEntity<?> getPseudonymByIdentifier(
            @PathVariable("domain") @NotBlank String domainName,
            @RequestParam(name = "id") @NotBlank String identifier,
            @RequestParam(name="idType") @NotBlank String idType) {
        return connector.getPseudonymByIdentifier(domainName, identifier, idType);
    }

    /**
     * Retrieves a pseudonym record by its pseudonym value.
     *
     * @param domainName The name of the domain where the pseudonym is stored (required, non-blank).
     * @param psn        The pseudonym value (required, non-blank).
     * @return A ResponseEntity containing a list of matching {@link PseudonymDto} objects.
     *         - 200 OK: Pseudonym record retrieved successfully.
     *         - 400 Bad Request: Invalid or missing required parameters.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No pseudonym found for the given pseudonym value.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @GetMapping("/domains/{domain}/pseudonym/by-psn")
    public ResponseEntity<?> getPseudonymByPsn(
            @PathVariable("domain") @NotBlank String domainName,
            @RequestParam(name="psn") @NotBlank String psn) {
        return connector.getPseudonymByPsn(domainName, psn);
    }

    /**
     * Updates a batch of pseudonym records in the specified domain.
     *
     * @param domainName       The name of the domain where pseudonyms are stored (required, non-blank).
     * @param pseudonymDtoList List of updated pseudonym data (required, validated).
     * @return A ResponseEntity indicating the update status.
     *         - 200 OK: Batch updated successfully.
     *         - 400 Bad Request: Invalid input data or missing required fields.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No pseudonyms found for the specified domain.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @PutMapping("/domains/{domain}/pseudonyms")
    public ResponseEntity<?> updatePseudonymBatch(
            @PathVariable("domain") String domainName,
            @RequestBody List<PseudonymDto> pseudonymDtoList) {
        return connector.updatePseudonymBatch(domainName, pseudonymDtoList);
    }

    /**
     * Updates a pseudonym record identified by its identifier and idType.
     *
     * @param domainName   The name of the domain where the pseudonym is stored (required, non-blank).
     * @param identifier   The identifier of the record (required, non-blank).
     * @param idType       The type of the identifier (required, non-blank).
     * @param pseudonymDto The updated pseudonym data (required, validated).
     * @return A ResponseEntity containing the updated {@link PseudonymDto} object.
     *         - 200 OK: Pseudonym updated successfully.
     *         - 400 Bad Request: Invalid input data or missing required fields.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No pseudonym found for the given identifier and idType.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @PutMapping("/{domain}/by-id")
    public ResponseEntity<?> updatePseudonymByIdentifier(
            @PathVariable("domain") String domainName,
            @RequestParam @NotBlank String identifier,
            @RequestParam @NotBlank String idType,
            @Valid @RequestBody PseudonymDto pseudonymDto) {
        return connector.updatePseudonymByIdentifier(domainName, identifier, idType, pseudonymDto);
    }

    /**
     * Updates a pseudonym record identified by its identifier and idType, replacing the entire record.
     *
     * @param domainName   The name of the domain where the pseudonym is stored (required, non-blank).
     * @param pseudonymDto The updated pseudonym data (required, validated).
     * @param identifier   The identifier of the record (required, non-blank).
     * @param idType       The type of the identifier (required, non-blank).
     * @return A ResponseEntity containing the updated {@link PseudonymDto} object.
     *         - 200 OK: Pseudonym updated successfully.
     *         - 400 Bad Request: Invalid input data or missing required fields.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No pseudonym found for the given identifier and idType.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @PutMapping("/domains/{domain}/pseudonym/complete/by-id")
    public ResponseEntity<?> updatePseudonymCompleteByIdentifier(
            @PathVariable("domain") String domainName,
            @RequestBody PseudonymDto pseudonymDto,
            @RequestParam(name = "id") @NotBlank String identifier,
            @RequestParam(name = "idType") @NotBlank String idType) {
        return connector.updatePseudonymCompleteByIdentifier(domainName, pseudonymDto, identifier, idType);
    }

    /**
     * Updates a pseudonym record identified by its pseudonym value, replacing the entire record.
     *
     * @param domainName   The name of the domain where the pseudonym is stored (required, non-blank).
     * @param pseudonymDto The updated pseudonym data (required, validated).
     * @param psn          The pseudonym value (required, non-blank).
     * @return A ResponseEntity containing the updated {@link PseudonymDto} object.
     *         - 200 OK: Pseudonym updated successfully.
     *         - 400 Bad Request: Invalid input data or missing required fields.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No pseudonym found for the given pseudonym value.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @PutMapping("/domains/{domain}/pseudonym/complete/by-psn")
    public ResponseEntity<?> updatePseudonymCompleteByPsn(
            @PathVariable("domain") String domainName,
            @RequestBody PseudonymDto pseudonymDto,
            @RequestParam(name = "psn") @NotBlank String psn) {
        return connector.updatePseudonymCompleteByPsn(domainName, pseudonymDto, psn);
    }

    /**
     * Updates a pseudonym record identified by its pseudonym value.
     *
     * @param domainName   The name of the domain where the pseudonym is stored (required, non-blank).
     * @param psn          The pseudonym value (required, non-blank).
     * @param pseudonymDto The updated pseudonym data (required, validated).
     * @return A ResponseEntity containing the updated {@link PseudonymDto} object.
     *         - 200 OK: Pseudonym updated successfully.
     *         - 400 Bad Request: Invalid input data or missing required fields.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No pseudonym found for the given pseudonym value.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @PutMapping("/{domain}/by-psn")
    public ResponseEntity<?> updatePseudonymByPsn(
            @PathVariable("domain") @NotBlank String domainName,
            @RequestParam @NotBlank String psn,
            @Valid @RequestBody PseudonymDto pseudonymDto) {
        return connector.updatePseudonymByPsn(domainName, psn, pseudonymDto);
    }

    /**
     * Deletes all pseudonym records in the specified domain.
     *
     * @param domainName The name of the domain from which to delete pseudonyms (required, non-blank).
     * @return A ResponseEntity indicating the deletion status.
     *         - 204 No Content: Batch deleted successfully.
     *         - 400 Bad Request: Invalid domain name.
     *         - 401 Unauthorized: Authentication failure.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @DeleteMapping("/domains/{domain}/pseudonyms")
    public ResponseEntity<?> deletePseudonymBatch(
            @PathVariable("domain") String domainName) {
        return connector.deletePseudonymBatch(domainName);
    }

    /**
     * Deletes a pseudonym record identified by its identifier, idType, or pseudonym value.
     *
     * @param domainName The name of the domain where the pseudonym is stored (required, non-blank).
     * @param identifier The identifier of the record (optional).
     * @param idType     The type of the identifier (optional).
     * @param psn        The pseudonym value (optional).
     * @throws IllegalArgumentException If neither identifier with idType nor psn is provided.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @DeleteMapping("/domains/{domain}/pseudonym")
    public void deletePseudonym(
            @PathVariable("domain") @NotBlank String domainName,
            @RequestParam(name = "id", required = false) String identifier,
            @RequestParam(name = "idtype", required = false) String idType,
            @RequestParam(name = "psn", required = false) String psn) {
        connector.deletePseudonym(domainName, identifier, idType, psn);
    }

    /**
     * Validates a pseudonym value in the specified domain.
     *
     * @param domainName The name of the domain where the pseudonym is stored (required, non-blank).
     * @param psn        The pseudonym value to validate (required, non-blank).
     * @return A ResponseEntity containing the validation result as a String.
     *         - 200 OK: Validation result returned successfully.
     *         - 400 Bad Request: Invalid or missing required parameters.
     *         - 401 Unauthorized: Authentication failure.
     *         - 404 Not Found: No pseudonym found for the given value.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the pseudonymization service fails to process the request.
     */
    @GetMapping("/domains/{domain}/pseudonym/validation")
    public ResponseEntity<?> validatePseudonym(
            @PathVariable("domain") String domainName,
            @RequestParam(name = "psn") String psn) {
        return connector.validatePseudonym(domainName, psn);
    }
}