package com.dekraspain.backend.template.modules.productOffering.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComplianceNamesDTO {

  private Long id;
  private String complianceName;
}
