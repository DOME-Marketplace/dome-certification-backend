package com.dekraspain.backend.template.modules.productOffering.application.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompliancesRequest {

  public Long profileId;
  public Long standardId;
}
