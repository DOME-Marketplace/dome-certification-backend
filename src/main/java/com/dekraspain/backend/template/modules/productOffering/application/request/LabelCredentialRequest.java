package com.dekraspain.backend.template.modules.productOffering.application.request;
import java.util.List;

import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStatesDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelCredentialRequest {
    private Long poId;
    private List<ComplianceCriteriaAndProfile> payload;
    private ProductOfferingStatesDTO data;
}
