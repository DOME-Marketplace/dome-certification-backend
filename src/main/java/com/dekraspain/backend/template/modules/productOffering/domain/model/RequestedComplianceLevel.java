package com.dekraspain.backend.template.modules.productOffering.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Embeddable
public class RequestedComplianceLevel {

  public static final String BASELINE = "Baseline";
  public static final String PROFESSIONAL = "Professional";
  public static final String PROFESSIONAL_PLUS = "Professional+";

  private static final List<String> ALLOWED_VALUES = Arrays.asList(
    BASELINE,
    PROFESSIONAL,
    PROFESSIONAL_PLUS
  );

  @Column(name = "requested_compliance_level", nullable = true)
  private String value;

  protected RequestedComplianceLevel() {
    // JPA
  }

  public RequestedComplianceLevel(String value) {
    if (!ALLOWED_VALUES.contains(value)) {
      throw new IllegalArgumentException("Invalid compliance level: " + value);
    }
    this.value = value;
  }

  // Maps the variants the Marketplace may send ("BL", "baseline", "PP", "Professional Plus", ...)
  // to the canonical value. Unknown or blank input falls back to Baseline: the requested level is
  // the provider's wish, not the outcome, so it must not make the whole intake fail.
  public static String normalize(String raw) {
    if (raw == null) return BASELINE;
    String v = raw.trim().toLowerCase().replace("_", " ").replace("-", " ");
    switch (v) {
      case "professional+":
      case "professional plus":
      case "pp":
        return PROFESSIONAL_PLUS;
      case "professional":
      case "p":
      case "pr":
        return PROFESSIONAL;
      default:
        return BASELINE;
    }
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestedComplianceLevel that = (RequestedComplianceLevel) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
