package com.dekraspain.backend.template.modules.productOffering.application.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComplianceCriteriaAndProfile {
    private Long complianceCriteriaId;
    private Long complianceProfileId;
}
