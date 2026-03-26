package com.dekraspain.backend.template.modules.productOffering.domain.service;

import com.dekraspain.backend.template.modules.productOffering.domain.model.ComplianceStandardsDTO;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceProfileEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.CompliancesStandarsEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceProfileRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceStandardRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ProductOfferingRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComplianceService {

  private final ComplianceRepository complianceRepository;
  private final ComplianceStandardRepository complianceStandardRepository;
  private final ComplianceProfileRepository complianceProfileRepository;
  private final ProductOfferingRepository productOfferingRepository;

  public ComplianceEntity createCompliance(
    Long standardId,
    Long profileId,
    Long productOfferingId
  ) {
    // Obtener entidades relacionadas
    CompliancesStandarsEntity standard = complianceStandardRepository
      .findById(standardId)
      .orElseThrow(() -> new EntityNotFoundException("Standard not found"));

    ComplianceProfileEntity profile = complianceProfileRepository
      .findById(profileId)
      .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

    ProductOfferingEntity productOffering = productOfferingRepository
      .findById(productOfferingId)
      .orElseThrow(() ->
        new EntityNotFoundException("Product Offering not found")
      );

    // Construir la entidad ComplianceEntity
    ComplianceEntity compliance = ComplianceEntity
      .builder()
      .compliancesStandard(standard)
      .complianceProfile(profile)
      .productOffering(productOffering)
      .build();

    // Guardar la entidad en la base de datos
    return complianceRepository.save(compliance);
  }

  public List<ComplianceStandardsDTO> getAllCompliances() {
    return complianceStandardRepository
      .findAll()
      .stream()
      .map(cs -> {
        return ComplianceStandardsDTO
          .builder()
          .id(cs.getId())
          .standard(cs.getStandard())
          .description(cs.getDescription())
          .build();
      })
      .collect(Collectors.toList());
  }

  // Obtener compliance standards por id
  public ComplianceStandardsDTO getComplianceStandardById(Long id) {
    return complianceStandardRepository
      .findById(id)
      .map(cs ->
        ComplianceStandardsDTO
          .builder()
          .id(cs.getId())
          .standard(cs.getStandard())
          .description(cs.getDescription())
          .build()
      )
      .orElse(null);
  }
}
