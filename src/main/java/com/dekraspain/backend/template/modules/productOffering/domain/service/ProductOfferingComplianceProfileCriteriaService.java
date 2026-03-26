
package com.dekraspain.backend.template.modules.productOffering.domain.service;

import com.dekraspain.backend.template.modules.productOffering.application.request.ComplianceCriteriaAndProfile;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.CompliancesCriteriaEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceProfileEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingComplianceProfileCriteriaEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ProductOfferingComplianceProfileCriteriaRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.CompliancesCriteriaRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceProfileRepository;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingComplianceProfileCriteriaDetailDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.CompilanceProfileDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.CompliancesCriteriaDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ProductOfferingComplianceProfileCriteriaService {

    private final ProductOfferingComplianceProfileCriteriaRepository repository;
    private final CompliancesCriteriaRepository compliancesCriteriaRepository;
    private final ComplianceProfileRepository complianceProfileRepository;

    public ProductOfferingComplianceProfileCriteriaEntity save(ProductOfferingComplianceProfileCriteriaEntity entity) {
        return repository.save(entity);
    }

    public List<ProductOfferingComplianceProfileCriteriaEntity> findAll() {
        return repository.findAll();
    }

    public Optional<ProductOfferingComplianceProfileCriteriaEntity> findById(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<ProductOfferingComplianceProfileCriteriaDetailDTO> findDetailsByProductOfferingId(Long productOfferingId) {
        List<ProductOfferingComplianceProfileCriteriaEntity> entities = repository.findAllByProductOffering_Id(productOfferingId);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return entities.stream().map(entity -> ProductOfferingComplianceProfileCriteriaDetailDTO.builder()
                .id(entity.getId())
                .productOfferingId(entity.getProductOffering().getId())
                .complianceCriteria(CompliancesCriteriaDTO.builder()
                        .id(entity.getCompliancesCriteria().getId())
                        .labelLevel(entity.getCompliancesCriteria().getLabelLevel())
                        .rulesVersion(entity.getCompliancesCriteria().getRulesVersion())
                        .category(entity.getCompliancesCriteria().getCategory())
                        .code(entity.getCompliancesCriteria().getCode())
                        .criteria(entity.getCompliancesCriteria().getCriteria())
                        .link(entity.getCompliancesCriteria().getLink())
                        .build())
                .complianceProfile(CompilanceProfileDTO.builder()
                        .id(entity.getComplianceProfile().getId())
                        .fileName(entity.getComplianceProfile().getFileName())
                        .url(entity.getComplianceProfile().getUrl())
                        .hash(entity.getComplianceProfile().getHash())
                        .build())
                .issuer(entity.getIssuer() != null ?
                        UserDTO.builder()
                                .id(entity.getIssuer().getId() != null ? entity.getIssuer().getId().toString() : null)
                                .username(entity.getIssuer().getUsername())
                                .firstname(entity.getIssuer().getFirstname())
                                .lastname(entity.getIssuer().getLastname())
                                .email(entity.getIssuer().getEmail())
                                .organization_country_code(entity.getIssuer().getCountry_code())
                                .organization_name(entity.getIssuer().getOrganization_name())
                                .last_seen(entity.getIssuer().getLast_seen())
                                .build()
                        : null)
                .createdAt(entity.getCreatedAt() != null ?
                        new java.sql.Timestamp(entity.getCreatedAt().getTime()).toLocalDateTime().format(formatter) : null)
                .build()
        ).toList();
}

    public void saveValidatedCriteria(ProductOfferingEntity productOffering, List<ComplianceCriteriaAndProfile> payload, UserEntity issuer) {
        if (payload == null) return;
        for (ComplianceCriteriaAndProfile item : payload) {
            CompliancesCriteriaEntity criteria = null;
            if (item.getComplianceCriteriaId() != null) {
                criteria = compliancesCriteriaRepository.findById(item.getComplianceCriteriaId()).orElse(null);
            }
            ComplianceProfileEntity profile = null;
            if (item.getComplianceProfileId() != null) {
                profile = complianceProfileRepository.findById(item.getComplianceProfileId()).orElse(null);
            }
            if (criteria != null && profile != null) {
                ProductOfferingComplianceProfileCriteriaEntity entity = ProductOfferingComplianceProfileCriteriaEntity.builder()
                    .productOffering(productOffering)
                    .compliancesCriteria(criteria)
                    .complianceProfile(profile)
                    .issuer(issuer)
                    .build();
                repository.save(entity);
            }
        }
    }
}
