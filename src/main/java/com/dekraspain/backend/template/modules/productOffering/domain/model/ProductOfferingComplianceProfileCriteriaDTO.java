package com.dekraspain.backend.template.modules.productOffering.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferingComplianceProfileCriteriaDTO {
    private Long id;
    private Long productOfferingId;
    private Long complianceCriteriaId;
    private Long complianceProfileId;
    private LocalDateTime createdAt;
    private String issuer;
}
