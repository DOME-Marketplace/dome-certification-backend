package com.dekraspain.backend.template.modules.productOffering.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dekraspain.backend.template.modules.productOffering.application.request.ProductOfferingRequest;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStates;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceProfileEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceProfileRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ProductOfferingRepository;
import com.dekraspain.backend.template.shared.email.service.EmailService;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ProductOfferingServiceIntakeTest {

  @Mock
  private ProductOfferingRepository productOfferingRepository;

  @Mock
  private ComplianceProfileRepository complianceProfileRepository;

  @Mock
  private EmailService emailService;

  @TempDir
  Path uploadDir;

  private ProductOfferingService service;

  @BeforeEach
  void setUp() {
    service = new ProductOfferingService(productOfferingRepository, complianceProfileRepository, emailService);
    ReflectionTestUtils.setField(service, "uploadDir", uploadDir.toString());
  }

  private static ProductOfferingRequest request() {
    return ProductOfferingRequest.builder()
        .id_PO("spec-1")
        .service_name("Service")
        .service_version("1")
        .requested_compliances_level("Baseline")
        .build();
  }

  private static MultipartFile pdf(String name, String content) {
    return new MockMultipartFile("files", name, "application/pdf", content.getBytes());
  }

  private static ProductOfferingEntity existing(long id, ProductOfferingStates status) {
    ProductOfferingEntity po = ProductOfferingEntity.builder().id_PO("spec-1").status(status).build();
    ReflectionTestUtils.setField(po, "id", id);
    return po;
  }

  private static ComplianceProfileEntity storedProfile(String hash) {
    return ComplianceProfileEntity.builder().hash(hash).build();
  }

  @Test
  void firstCallCreatesARequest() throws Exception {
    when(productOfferingRepository.findByIdPoNewestFirst("spec-1")).thenReturn(List.of());

    ProductOfferingService.IntakeResult result =
        service.createProductOfferingM2M(request(), List.of(pdf("form.pdf", "v1")));

    assertTrue(result.created());
    assertEquals(1, result.filesAdded());
    verify(productOfferingRepository).save(any(ProductOfferingEntity.class));
    ArgumentCaptor<ComplianceProfileEntity> saved = ArgumentCaptor.forClass(ComplianceProfileEntity.class);
    verify(complianceProfileRepository).save(saved.capture());
    assertEquals("form.pdf", saved.getValue().getFileName());
    assertNotNull(saved.getValue().getUploadedAt());
  }

  @Test
  void retryWithIdenticalFilesWhileInProgressIsANoOp() throws Exception {
    ProductOfferingEntity inProgress = existing(7L, ProductOfferingStates.IN_PROGRESS);
    when(productOfferingRepository.findByIdPoNewestFirst("spec-1")).thenReturn(List.of(inProgress));
    // SHA-256 of "v1"
    when(complianceProfileRepository.findByProductOfferingId(7L)).thenReturn(
        List.of(storedProfile("3bfc269594ef649228e9a74bab00f042efc91d5acc6fbee31a382e80d42388fe")));

    ProductOfferingService.IntakeResult first =
        service.createProductOfferingM2M(request(), List.of(pdf("form.pdf", "v1")));
    assertFalse(first.created());
    assertSame(inProgress, first.productOffering());
    assertEquals(0, first.filesAdded());
    assertEquals(1, first.filesSkipped());
    verify(productOfferingRepository, never()).save(any(ProductOfferingEntity.class));
    verify(complianceProfileRepository, never()).save(any(ComplianceProfileEntity.class));
  }

  @Test
  void identicalFileIsSkippedAndNewDocumentIsAddedToTheRequestInProgress() throws Exception {
    ProductOfferingEntity inProgress = existing(7L, ProductOfferingStates.IN_PROGRESS);
    when(productOfferingRepository.findByIdPoNewestFirst("spec-1")).thenReturn(List.of(inProgress));
    when(complianceProfileRepository.findByProductOfferingId(7L)).thenReturn(List.of());

    // first upload of "form.pdf" content v1 lands; the same bytes again are skipped; a corrected
    // version with the same name is a different file and is added
    ProductOfferingService.IntakeResult result = service.createProductOfferingM2M(
        request(), List.of(pdf("form.pdf", "v1"), pdf("form.pdf", "v1"), pdf("form.pdf", "v2")));

    assertFalse(result.created());
    assertEquals(2, result.filesAdded());
    assertEquals(1, result.filesSkipped());
    verify(complianceProfileRepository, times(2)).save(any(ComplianceProfileEntity.class));
    verify(productOfferingRepository, never()).save(any(ProductOfferingEntity.class));
  }

  @Test
  void resubmissionAfterRejectionOpensANewRequest() throws Exception {
    when(productOfferingRepository.findByIdPoNewestFirst("spec-1"))
        .thenReturn(List.of(existing(7L, ProductOfferingStates.REJECTED)));

    ProductOfferingService.IntakeResult result =
        service.createProductOfferingM2M(request(), List.of(pdf("form.pdf", "corrected")));

    assertTrue(result.created());
    assertEquals(1, result.filesAdded());
    verify(productOfferingRepository).save(any(ProductOfferingEntity.class));
  }
}
