package com.dekraspain.backend.template.modules.productOffering.persistence.jpa;

import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplianceRepository
  extends JpaRepository<ComplianceEntity, Long> {
  List<ComplianceEntity> findByProductOfferingId(Long productOfferingId);
}
