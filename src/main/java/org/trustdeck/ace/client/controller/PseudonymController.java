package org.trustdeck.ace.client.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.trustdeck.ace.client.service.PseudonymizationConnector;
import org.trustdeck.ace.client.dto.RecordDto;

import java.util.List;

/**
 * REST controller for pseudonymization operations.
 */
@RestController
@RequestMapping("/api/pseudonyms")
@Validated
public class PseudonymController {
    private final PseudonymizationConnector connector;

    public PseudonymController(PseudonymizationConnector connector) {
        this.connector = connector;
    }

    /**
     * Creates a new pseudonym in the specified domain.
     * @param domainName The name of the domain
     * @param recordDto The record data to create
     * @param omitPrefix Whether to omit the domain prefix
     * @return List of created RecordDto objects
     */
    @PostMapping("/{domainName}")
    public List<RecordDto> createPseudonym(
            @PathVariable @NotBlank String domainName,
            @Valid @RequestBody RecordDto recordDto,
            @RequestParam(defaultValue = "false") boolean omitPrefix) {
        return connector.createPseudonym(domainName, recordDto, omitPrefix);
    }

    /**
     * Retrieves a pseudonym by identifier and idType.
     * @param domainName The name of the domain
     * @param identifier The identifier of the record
     * @param idType The type of the identifier
     * @return List of matching RecordDto objects
     */
    @GetMapping("/{domainName}/by-id")
    public List<RecordDto> getPseudonymByIdentifier(
            @PathVariable @NotBlank String domainName,
            @RequestParam @NotBlank String identifier,
            @RequestParam @NotBlank String idType) {
        return connector.getPseudonymByIdentifier(domainName, identifier, idType);
    }

    /**
     * Retrieves a pseudonym by pseudonym value.
     * @param domainName The name of the domain
     * @param psn The pseudonym value
     * @return List of matching RecordDto objects
     */
    @GetMapping("/{domainName}/by-psn")
    public List<RecordDto> getPseudonymByPsn(
            @PathVariable @NotBlank String domainName,
            @RequestParam @NotBlank String psn) {
        return connector.getPseudonymByPsn(domainName, psn);
    }

    /**
     * Updates a pseudonym by identifier and idType.
     * @param domainName The name of the domain
     * @param identifier The identifier of the record
     * @param idType The type of the identifier
     * @param recordDto The updated record data
     * @return The updated RecordDto object
     */
    @PutMapping("/{domainName}/by-id")
    public RecordDto updatePseudonymByIdentifier(
            @PathVariable @NotBlank String domainName,
            @RequestParam @NotBlank String identifier,
            @RequestParam @NotBlank String idType,
            @Valid @RequestBody RecordDto recordDto) {
        return connector.updatePseudonymByIdentifier(domainName, identifier, idType, recordDto);
    }

    /**
     * Updates a pseudonym by pseudonym value.
     * @param domainName The name of the domain
     * @param psn The pseudonym value
     * @param recordDto The updated record data
     * @return The updated RecordDto object
     */
    @PutMapping("/{domainName}/by-psn")
    public RecordDto updatePseudonymByPsn(
            @PathVariable @NotBlank String domainName,
            @RequestParam @NotBlank String psn,
            @Valid @RequestBody RecordDto recordDto) {
        return connector.updatePseudonymByPsn(domainName, psn, recordDto);
    }

    /**
     * Deletes a pseudonym by identifier, idType, or pseudonym.
     * @param domainName The name of the domain
     * @param identifier The identifier of the record (optional)
     * @param idType The type of the identifier (optional)
     * @param psn The pseudonym value (optional)
     */
    @DeleteMapping("/{domainName}")
    public void deletePseudonym(
            @PathVariable @NotBlank String domainName,
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) String idType,
            @RequestParam(required = false) String psn) {
        connector.deletePseudonym(domainName, identifier, idType, psn);
    }
}