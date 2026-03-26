package com.dekraspain.backend.template.modules.productOffering.persistence.jpa;

import com.dekraspain.backend.template.modules.productOffering.persistence.entity.CompliancesCriteriaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompliancesCriteriaRepository
  extends JpaRepository<CompliancesCriteriaEntity, Long> {
  boolean existsByCodeAndRulesVersion(String code, String rulesVersion);

  List<CompliancesCriteriaEntity> findByLabelLevel(String labelLevel);
}
