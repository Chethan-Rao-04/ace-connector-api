package org.trustdeck.ace.client.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Data Transfer Object (DTO) for pseudonymization records, matching the pseudonymization service's data model.
 */
@Getter
@Setter
public class PseudonymDto {
    @NotBlank
    private String id; // Identifier of the record
    @NotBlank
    private String idType; // Type of the identifier
    private String psn; // Pseudonym value
    private Timestamp validFrom; // Start of validity period
    private Boolean validFromInherited; // Whether validFrom is inherited from domain
    private Timestamp validTo; // End of validity period
    private Boolean validToInherited; // Whether validTo is inherited from domain
    private String validityTime; // Validity period as a string (e.g., "1d")
    private String domainName; // Name of the domain
}