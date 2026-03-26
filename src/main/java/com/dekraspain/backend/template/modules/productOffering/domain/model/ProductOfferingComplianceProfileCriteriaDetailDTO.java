package com.dekraspain.backend.template.modules.productOffering.domain.model;

import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferingComplianceProfileCriteriaDetailDTO {
    private Long id;
    private Long productOfferingId;
    private CompliancesCriteriaDTO complianceCriteria;
    private CompilanceProfileDTO complianceProfile;
    private UserDTO issuer;
    private String createdAt;
}
