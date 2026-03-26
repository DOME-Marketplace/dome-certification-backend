package com.dekraspain.backend.template.modules.productOffering.persistence.jpa;

import com.dekraspain.backend.template.modules.productOffering.persistence.entity.CompliancesStandarsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplianceStandardRepository
  extends JpaRepository<CompliancesStandarsEntity, Long> {}
