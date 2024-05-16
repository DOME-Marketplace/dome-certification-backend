package com.dekraspain.backend.template.modules.productOffering.persistence.jpa;

import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceProfileEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceProfileRepository
  extends JpaRepository<ComplianceProfileEntity, Long> {
  List<ComplianceProfileEntity> findByProductOfferingId(Long productOfferingId);
}
