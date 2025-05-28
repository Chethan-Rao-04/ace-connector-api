package org.trustdeck.ace.client.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.trustdeck.ace.client.dto.DomainDto;
import org.trustdeck.ace.client.service.DomainConnector;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * REST controller for domain management operations.
 */
@RestController
@RequestMapping("/api/pseudonymization")
@Validated
public class DomainController {
    private final DomainConnector connector;

    public DomainController(DomainConnector connector) {
        this.connector = connector;
    }

    /**
     * Retrieves the domain hierarchy in a minimal representation.
     * This endpoint is marked as experimental.
     *
     * @return A ResponseEntity containing a list of domains in hierarchical structure.
     *         - 200 OK: Domain hierarchy retrieved successfully.
     *         - 401 Unauthorized: Authentication failure.
     *         - 500 Internal Server Error: Service failure or unexpected error.
     * @throws RuntimeException If the service fails to process the request.
     */

    @GetMapping("/domain/hierarchy")
    public List<DomainDto> getDomainHierarchy() {
        return connector.getAllDomains();
    }


    /**
     * Gets a domain by name.
     * @param domainName The name of the domain
     * @return The requested DomainDto
     */
    @GetMapping("/domain")
    public DomainDto getDomain(@RequestParam(name = "name") @NotBlank String domainName) {
        return connector.getDomain(domainName);
    }



    /**
     * Gets a specific attribute of a domain.
     * @param domainName The name of the domain
     * @param attributeName The name of the attribute to retrieve
     * @return The requested DomainDto containing only the specified attribute
     */
    @GetMapping("/domains/{domain}/{attribute}")
    public DomainDto getDomainAttribute(
            @PathVariable("domain") @NotBlank String domainName,
            @PathVariable("attribute") @NotBlank String attributeName) {
        return connector.getDomainAttribute(domainName, attributeName);
    }

    /**
     * Creates a new domain with a reduced set of attributes.
     * @param domainDto The domain to create
     * @return The created DomainDto
     */
    @PostMapping("/domain")
    public DomainDto createDomain(@Valid @RequestBody DomainDto domainDto) {
        return connector.createDomain(domainDto);
    }

    /**
     * Creates a new domain with all attributes.
     * @param domainDto The domain to create
     * @return The created DomainDto
     */
    @PostMapping("/domain/complete")
    public DomainDto createDomainComplete(@Valid @RequestBody DomainDto domainDto) {
        return connector.createDomainComplete(domainDto);
    }

    /**
     * Updates an existing domain with a reduced set of attributes.
     * @param oldDomainName The name of the domain to update
     * @param domainDto The updated domain data
     * @return The updated DomainDto
     */
    @PutMapping("/domain")
    public DomainDto updateDomain(
            @RequestParam(name = "name", required = true) String oldDomainName,
            @Valid @RequestBody DomainDto domainDto) {
        return connector.updateDomain(oldDomainName, domainDto);
    }

    /**
     * Updates an existing domain with all attributes.
     * @param domainName The name of the domain to update
     * @param domainDto The updated domain data
     * @param recursive Whether to apply changes recursively to sub-domains
     * @return The updated DomainDto
     */
    @PutMapping("/domain/complete/{domain}")
    public DomainDto updateDomainComplete(
            @PathVariable("domain") @NotBlank String domainName,
            @Valid @RequestBody DomainDto domainDto,
            @RequestParam(defaultValue = "true") boolean recursive) {
        return connector.updateDomainComplete(domainName, domainDto, recursive);
    }


    /**
     * Updates the salt of a domain.
     * @param domainName The name of the domain
     * @param newSalt The new salt value
     * @param allowEmpty Whether to allow an empty salt
     * @return The updated DomainDto
     */
    @PutMapping("/domains/{domain}/salt")
    public DomainDto updateSalt(
            @PathVariable("domain") @NotBlank String domainName,
            @RequestParam(name="new-salt") @NotBlank String newSalt,
            @RequestParam(defaultValue = "false") boolean allowEmpty) {
        return connector.updateSalt(domainName, newSalt, allowEmpty);
    }


    /**
     * Deletes a domain.
     * @param domainName The name of the domain to delete
     * @param recursive Whether to delete sub-domains recursively
     */
    @DeleteMapping("/domain/{domain}")
    public void deleteDomain(
            @PathVariable("domain") @NotBlank String domainName,
            @RequestParam(name = "recursive",defaultValue = "true") boolean recursive) {
        connector.deleteDomain(domainName, recursive);
    }


}