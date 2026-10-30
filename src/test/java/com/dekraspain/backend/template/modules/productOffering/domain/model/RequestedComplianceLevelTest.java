package com.dekraspain.backend.template.modules.productOffering.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RequestedComplianceLevelTest {

  @Test
  void normalizeMapsMarketplaceVariantsToCanonicalValues() {
    assertEquals("Baseline", RequestedComplianceLevel.normalize("Baseline"));
    assertEquals("Baseline", RequestedComplianceLevel.normalize("BL"));
    assertEquals("Baseline", RequestedComplianceLevel.normalize(" baseline "));
    assertEquals("Professional", RequestedComplianceLevel.normalize("P"));
    assertEquals("Professional", RequestedComplianceLevel.normalize("professional"));
    assertEquals("Professional+", RequestedComplianceLevel.normalize("Professional+"));
    assertEquals("Professional+", RequestedComplianceLevel.normalize("PP"));
    assertEquals("Professional+", RequestedComplianceLevel.normalize("Professional Plus"));
    assertEquals("Professional+", RequestedComplianceLevel.normalize("professional_plus"));
  }

  @Test
  void normalizeFallsBackToBaselineForBlankOrUnknown() {
    assertEquals("Baseline", RequestedComplianceLevel.normalize(null));
    assertEquals("Baseline", RequestedComplianceLevel.normalize(""));
    assertEquals("Baseline", RequestedComplianceLevel.normalize("gold"));
  }

  @Test
  void constructorStaysStrict() {
    assertThrows(IllegalArgumentException.class, () -> new RequestedComplianceLevel("BL"));
  }
}
