package com.dekraspain.backend.template.modules.productOffering.application.request;

import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStatesDTO;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuanceRequest {

  private Long poId;
  private Map<String, Object> payload;
  private String idToken;
  private ProductOfferingStatesDTO data;
}
