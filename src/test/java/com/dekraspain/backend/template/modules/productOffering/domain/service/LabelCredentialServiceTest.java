package com.dekraspain.backend.template.modules.productOffering.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dekraspain.backend.template.modules.productOffering.application.request.ComplianceCriteriaAndProfile;
import com.dekraspain.backend.template.modules.productOffering.domain.model.LabelCredentialPayloadDTO;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceProfileEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.CompliancesCriteriaEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceProfileRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.CompliancesCriteriaRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ProductOfferingRepository;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the label-level plumbing: a non-blank level passed in is stamped verbatim on the
 * credential, an absent/blank level defaults to "BL" (defensive — never issues a null level), and
 * the compliant-credential / validated-criteria lists are built from the referenced criteria +
 * profiles. No Spring context, DB, or external services.
 */
@ExtendWith(MockitoExtension.class)
class LabelCredentialServiceTest {

  @Mock private ProductOfferingRepository productOfferingRepository;
  @Mock private CompliancesCriteriaRepository compliancesCriteriaRepository;
  @Mock private ComplianceProfileRepository complianceProfileRepository;

  @InjectMocks private LabelCredentialService service;

  private LabelCredentialPayloadDTO generateWithLevel(String labelLevel) {
    ProductOfferingEntity po = mock(ProductOfferingEntity.class);
    when(po.getId_PO()).thenReturn("prod-1");
    when(productOfferingRepository.findById(1L)).thenReturn(Optional.of(po));

    UserEntity user = mock(UserEntity.class);
    lenient().when(user.getOrganization_id()).thenReturn("VATPT-508245567");

    CompliancesCriteriaEntity criteria = mock(CompliancesCriteriaEntity.class);
    when(criteria.getCode()).thenReturn("DP-1");
    when(criteria.getCategory()).thenReturn("DATA PROTECTION & MANAGEMENT");
    when(criteria.getLink()).thenReturn("https://example.org/#P2.1.1");
    when(criteria.getRulesVersion()).thenReturn("CD25.03");
    when(compliancesCriteriaRepository.findById(10L)).thenReturn(Optional.of(criteria));

    ComplianceProfileEntity profile = mock(ComplianceProfileEntity.class);
    when(profile.getHash()).thenReturn("sha256-abc");
    when(complianceProfileRepository.findById(20L)).thenReturn(Optional.of(profile));

    ComplianceCriteriaAndProfile item =
        ComplianceCriteriaAndProfile.builder().complianceCriteriaId(10L).complianceProfileId(20L).build();

    return service.generateLabelCredentialPayload(
        1L, List.of(item), user, "2027-01-01T00:00:00.000Z", labelLevel);
  }

  @Test
  void stampsProfessionalLevelVerbatim() {
    LabelCredentialPayloadDTO payload = generateWithLevel("P");
    assertEquals("P", payload.getCredentialSubject().getGxLabelLevel());
  }

  @Test
  void stampsProfessionalPlusLevelVerbatim() {
    LabelCredentialPayloadDTO payload = generateWithLevel("PP");
    assertEquals("PP", payload.getCredentialSubject().getGxLabelLevel());
  }

  @Test
  void defaultsNullLevelToBaseline() {
    LabelCredentialPayloadDTO payload = generateWithLevel(null);
    assertEquals("BL", payload.getCredentialSubject().getGxLabelLevel());
  }

  @Test
  void defaultsBlankLevelToBaseline() {
    LabelCredentialPayloadDTO payload = generateWithLevel("   ");
    assertEquals("BL", payload.getCredentialSubject().getGxLabelLevel());
  }

  @Test
  void buildsCompliantCredentialsAndValidatedCriteriaFromReferencedCriteria() {
    LabelCredentialPayloadDTO.CredentialSubject subject = generateWithLevel("BL").getCredentialSubject();

    assertEquals("BL", subject.getGxLabelLevel());
    assertEquals(1, subject.getGxCompliantCredentials().size());

    LabelCredentialPayloadDTO.CompliantCredential cc = subject.getGxCompliantCredentials().get(0);
    assertEquals("urn:criteria:dp-1", cc.getId());
    assertEquals("gx:DataProtection", cc.getType());
    assertEquals("sha256-abc", cc.getGxDigestSRI());

    assertTrue(subject.getGxValidatedCriteria().contains("https://example.org/#P2.1.1"));
    assertEquals("CD25.03", subject.getGxRulesVersion());
  }
}
