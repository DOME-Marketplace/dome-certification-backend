package com.dekraspain.backend.template.modules.productOffering.persistence.jpa;

import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import jakarta.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOfferingRepository
  extends JpaRepository<ProductOfferingEntity, Long> {
  @Query(
    "SELECT po.id FROM ProductOfferingEntity po WHERE po.isExpirationEmailSent = false  AND po.expiration_date <= CURRENT_DATE"
  )
  List<Long> findExpiredProductOfferingIds();

  @Query(
    "SELECT po.id FROM ProductOfferingEntity po WHERE po.status != 'EXPIRED' AND po.isExpirationWarningEmailSent = false AND po.expiration_date > :twoMonthsAgoDate"
  )
  List<Long> findProductOfferingIdsExpiringBefore(
    @Param("twoMonthsAgoDate") Date twoMonthsAgoDate
  );

  default List<Long> findProductOfferingIdsExpiringSoon() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, -2);
    Date twoMonthsAgoDate = calendar.getTime();

    return findProductOfferingIdsExpiringBefore(twoMonthsAgoDate);
  }

  @Transactional
  @Modifying
  @Query(
    "UPDATE ProductOfferingEntity po SET po.isExpirationEmailSent = true WHERE po.id = :id"
  )
  void updateIsExpirationEmailSent(@Param("id") Long id);

  @Transactional
  @Modifying
  @Query(
    "UPDATE ProductOfferingEntity po SET po.isExpirationWarningEmailSent = true WHERE po.id = :id"
  )
  void updateIsExpirationWarningEmailSent(@Param("id") Long id);

  // Consulta personalizada para filtrar por el id de issuer
  @Query("SELECT p FROM ProductOfferingEntity p WHERE p.issuer.id = :issuerId")
  List<ProductOfferingEntity> findAllByIssuerId(UUID issuerId);
}
