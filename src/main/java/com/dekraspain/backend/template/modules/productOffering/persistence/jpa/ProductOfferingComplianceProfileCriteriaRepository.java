package com.dekraspain.backend.template.modules.productOffering.persistence.jpa;

import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingComplianceProfileCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductOfferingComplianceProfileCriteriaRepository extends JpaRepository<ProductOfferingComplianceProfileCriteriaEntity, Long> {
    List<ProductOfferingComplianceProfileCriteriaEntity> findAllByProductOffering_Id(Long productOfferingId);
}
