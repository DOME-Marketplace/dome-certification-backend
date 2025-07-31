package com.dekraspain.backend.template.modules.productOffering.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompliancesCriteriaDTO {
    private Long id;
    private String labelLevel;
    private String rulesVersion;
    private String category;
    private String code;
    private String criteria;
    private String link;
}
