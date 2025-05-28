package org.trustdeck.ace.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for domains in the pseudonymization service.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainDto {
    private Integer id;
    private String name;
    private String prefix;
    private LocalDateTime validFrom;
    private Boolean validFromInherited;
    private LocalDateTime validTo;
    private String validityTime;
    private Boolean validToInherited;
    private Boolean enforceStartDateValidity;
    private Boolean enforceStartDateValidityInherited;
    private Boolean enforceEndDateValidity;
    private Boolean enforceEndDateValidityInherited;
    private String algorithm;
    private Boolean algorithmInherited;
    private String alphabet;
    private Boolean alphabetInherited;
    private Long randomAlgorithmDesiredSize;
    private Boolean randomAlgorithmDesiredSizeInherited;
    private Double randomAlgorithmDesiredSuccessProbability;
    private Boolean randomAlgorithmDesiredSuccessProbabilityInherited;
    private Boolean multiplePsnAllowed;
    private Boolean multiplePsnAllowedInherited;
    private Long consecutiveValueCounter;
    private Integer pseudonymLength;
    private Boolean pseudonymLengthInherited;
    private Character paddingCharacter;
    private Boolean paddingCharacterInherited;
    private Boolean addCheckDigit;
    private Boolean addCheckDigitInherited;
    private Boolean lengthIncludesCheckDigit;
    private Boolean lengthIncludesCheckDigitInherited;
    private String salt;
    private Integer saltLength;
    private String description;
    private Integer superDomainID;
    private String superDomainName;
}
