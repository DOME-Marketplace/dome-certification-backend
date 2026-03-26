package com.dekraspain.backend.template.modules.productOffering.application.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifierTokenResponse {

  private String access_token;
  private String token_type;
  private Integer expires_in;
}
