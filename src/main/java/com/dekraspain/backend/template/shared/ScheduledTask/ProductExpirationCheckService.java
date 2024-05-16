package com.dekraspain.backend.template.shared.ScheduledTask;

import com.dekraspain.backend.template.modules.productOffering.domain.service.ProductOfferingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ProductExpirationCheckService {

  @Autowired
  private ProductOfferingService productOfferingService;

  private static final Logger logger = LoggerFactory.getLogger(
    ProductExpirationCheckService.class
  );

  @Scheduled(cron = "55 23 23 * * *")
  public void checkProductExpiration() {
    productOfferingService.checkProductExpiration();
    logger.info("Running scheduled task...");
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void checkProductExpirationWarning() {
    productOfferingService.checkProductWaringExpiration();
    logger.info("Running scheduled task...");
  }
}
