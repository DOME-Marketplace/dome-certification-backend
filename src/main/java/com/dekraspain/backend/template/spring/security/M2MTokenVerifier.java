package com.dekraspain.backend.template.spring.security;

public interface M2MTokenVerifier {
  /**
   * Verify the provided JWT token signature and basic claims. Returns a detailed result.
   */
  M2MTokenVerificationResult verify(String jwt);
}
