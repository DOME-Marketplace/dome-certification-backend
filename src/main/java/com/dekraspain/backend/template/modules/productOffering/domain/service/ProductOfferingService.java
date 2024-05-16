package com.dekraspain.backend.template.modules.productOffering.domain.service;

import com.dekraspain.backend.template.modules.productOffering.application.request.ProductOfferingRequest;
import com.dekraspain.backend.template.modules.productOffering.domain.model.CompilanceProfileDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ComplianceNamesDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStates;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStatesDTO;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceProfileEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceProfileRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ProductOfferingRepository;
import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.shared.email.service.EmailService;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductOfferingService {

  @Value("${file.upload-dir}")
  private String uploadDir;

  private final ProductOfferingRepository productOfferingRepository;
  private final ComplianceProfileRepository complianceProfileRepository;
  private final EmailService emailService;
  private final ComplianceRepository complianceRepository;

  private static final Logger logger = LoggerFactory.getLogger(
    ProductOfferingService.class
  );

  public void createProductOffering(
    ProductOfferingRequest request,
    List<MultipartFile> files
  ) throws IOException {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();

    if (authentication == null) {
      return;
    }
    UserEntity user = (UserEntity) authentication.getPrincipal();

    ProductOfferingEntity productOffering = ProductOfferingEntity
      .builder()
      .service_name(request.getService_name())
      .service_version(request.getService_version())
      .name_organization(request.getName_organization())
      .address_organization(request.getAddress_organization())
      .ISO_Country_Code(request.getISO_Country_Code())
      .url_organization(request.getUrl_organization())
      .email_organization(request.getEmail_organization())
      .id_PO(request.getId_PO())
      .status(ProductOfferingStates.IN_PROGRESS)
      .issuer(user)
      .request_date(new Date())
      .issue_date(null)
      .expiration_date(null)
      .isExpirationEmailSent(false)
      .isExpirationWarningEmailSent(false)
      .build();

    productOfferingRepository.save(productOffering);

    Path uploadPath = Paths.get(uploadDir);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    for (MultipartFile file : files) {
      // Guardar el archivo en el almacenamiento de Spring Boot
      String fileName = StringUtils.cleanPath(file.getOriginalFilename());
      Path staticFilePath = uploadPath.resolve(fileName);
      Files.copy(
        file.getInputStream(),
        staticFilePath,
        StandardCopyOption.REPLACE_EXISTING
      );

      String fileDownloadUri = uploadDir + "/" + fileName;
      // Crear una nueva instancia de ComplianceProfileEntity para almacenar el archivo
      ComplianceProfileEntity complianceProfile = ComplianceProfileEntity
        .builder()
        .fileName(fileName)
        .productOffering(productOffering)
        .url(fileDownloadUri) // Usar la URI de descarga generada
        .build();

      // Guardar el ComplianceProfileEntity en la base de datos
      complianceProfileRepository.save(complianceProfile);
    }
  }

  public ProductOfferingDTO updateStatusProductOffering(
    Long id,
    ProductOfferingStatesDTO status
  ) {
    ProductOfferingEntity existingProductOffering = getProductOfferingById(id);
    existingProductOffering.setStatus(status.getStatus());
    existingProductOffering.setIssue_date(new Date());

    if (status.getStatus() == ProductOfferingStates.VALIDATED) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date());
      calendar.add(Calendar.YEAR, 2);
      Date expirationDate = calendar.getTime();
      existingProductOffering.setExpiration_date(expirationDate);

      Optional<List<String>> compliances = status.getCompliances();

      for (String compliance : compliances.get()) {
        ComplianceEntity complianceEntity = ComplianceEntity
          .builder()
          .complianceName(compliance)
          .productOffering(existingProductOffering)
          .build();
        complianceRepository.save(complianceEntity);
      }
    }
    ProductOfferingEntity updatedProductOffering = productOfferingRepository.save(
      existingProductOffering
    );

    UserEntity issuer = updatedProductOffering.getIssuer();

    List<CompilanceProfileDTO> complianceProfileDTOs = updatedProductOffering
      .getComplianceProfiles()
      .stream()
      .map(cp ->
        CompilanceProfileDTO
          .builder()
          .id(cp.getId())
          .fileName(cp.getFileName())
          .url(cp.getUrl())
          .build()
      )
      .collect(Collectors.toList());

    // List<ComplianceEntity> compliances = updatedProductOffering.getCompliances();

    List<ComplianceNamesDTO> compliances = updatedProductOffering
      .getCompliances()
      .stream()
      .map(c ->
        ComplianceNamesDTO
          .builder()
          .id(c.getId())
          .complianceName(c.getComplianceName())
          .build()
      )
      .collect(Collectors.toList());

    UserDTO issuerDTO = UserDTO
      .builder()
      .id(issuer.getId().toString())
      .username(issuer.getUsername())
      .firstname(issuer.getFirstname())
      .lastname(issuer.getLastname())
      .country_code(issuer.getCountry_code())
      .last_seen(issuer.getLast_seen())
      .address(issuer.getAddress())
      .organization_name(issuer.getOrganization_name())
      .website(issuer.getWebsite())
      .build();

    return ProductOfferingDTO
      .builder()
      .id(updatedProductOffering.getId())
      .service_name(updatedProductOffering.getService_name())
      .service_version(updatedProductOffering.getService_version())
      .name_organization(updatedProductOffering.getName_organization())
      .address_organization(updatedProductOffering.getAddress_organization())
      .ISO_Country_Code(updatedProductOffering.getISO_Country_Code())
      .url_organization(updatedProductOffering.getUrl_organization())
      .email_organization(updatedProductOffering.getEmail_organization())
      .id_PO(updatedProductOffering.getId_PO())
      .status(updatedProductOffering.getStatus())
      .request_date(updatedProductOffering.getRequest_date())
      .issue_date(updatedProductOffering.getIssue_date())
      .issuer(issuerDTO)
      .expiration_date(updatedProductOffering.getExpiration_date())
      .image(updatedProductOffering.getImage())
      .complianceProfiles(complianceProfileDTOs)
      .compliances(compliances)
      .build();
  }

  public void deleteProductOffering(Long id) {
    ProductOfferingEntity productOffering = getProductOfferingById(id);
    productOfferingRepository.delete(productOffering);
  }

  public ProductOfferingEntity getProductOfferingById(Long id) {
    return productOfferingRepository.findById(id).orElseThrow();
  }

  public List<ProductOfferingDTO> getAllProductOffering() {
    List<ProductOfferingDTO> productOfferings = productOfferingRepository
      .findAll()
      .stream()
      .map(productOffering ->
        ProductOfferingDTO
          .builder()
          .id(productOffering.getId())
          .id_PO(productOffering.getId_PO())
          .service_name(productOffering.getService_name())
          .service_version(productOffering.getService_version())
          .name_organization(productOffering.getName_organization())
          .address_organization(productOffering.getAddress_organization())
          .ISO_Country_Code(productOffering.getISO_Country_Code())
          .url_organization(productOffering.getUrl_organization())
          .email_organization(productOffering.getEmail_organization())
          .status(productOffering.getStatus())
          .issuer(
            UserDTO
              .builder()
              .id(productOffering.getIssuer().getId().toString())
              .username(productOffering.getIssuer().getUsername())
              .firstname(productOffering.getIssuer().getFirstname())
              .lastname(productOffering.getIssuer().getLastname())
              .country_code(productOffering.getIssuer().getCountry_code())
              .last_seen(productOffering.getIssuer().getLast_seen())
              .address(productOffering.getIssuer().getAddress())
              .organization_name(
                productOffering.getIssuer().getOrganization_name()
              )
              .website(productOffering.getIssuer().getWebsite())
              .build()
          )
          .complianceProfiles(
            productOffering
              .getComplianceProfiles()
              .stream()
              .map(cp ->
                CompilanceProfileDTO
                  .builder()
                  .id(cp.getId())
                  .fileName(cp.getFileName())
                  .url(cp.getUrl())
                  .build()
              )
              .collect(Collectors.toList())
          )
          .issue_date(productOffering.getIssue_date())
          .expiration_date(productOffering.getExpiration_date())
          .request_date(productOffering.getRequest_date())
          .image(productOffering.getImage())
          .request_date(productOffering.getRequest_date())
          .issue_date(productOffering.getIssue_date())
          .expiration_date(productOffering.getExpiration_date())
          .compliances(
            productOffering
              .getCompliances()
              .stream()
              .map(c ->
                ComplianceNamesDTO
                  .builder()
                  .id(c.getId())
                  .complianceName(c.getComplianceName())
                  .build()
              )
              .collect(Collectors.toList())
          )
          .build()
      )
      .collect(Collectors.toList());

    return productOfferings;
  }

  public List<ProductOfferingDTO> getAllProductOfferingByUserId() {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();

    // if (authentication == null) {
    //   return;
    // }

    UserEntity user = (UserEntity) authentication.getPrincipal();

    List<ProductOfferingDTO> productOfferings = productOfferingRepository
      .findAllByIssuerId(user.id)
      .stream()
      .map(productOffering ->
        ProductOfferingDTO
          .builder()
          .id(productOffering.getId())
          .id_PO(productOffering.getId_PO())
          .service_name(productOffering.getService_name())
          .service_version(productOffering.getService_version())
          .name_organization(productOffering.getName_organization())
          .address_organization(productOffering.getAddress_organization())
          .ISO_Country_Code(productOffering.getISO_Country_Code())
          .url_organization(productOffering.getUrl_organization())
          .email_organization(productOffering.getEmail_organization())
          .status(productOffering.getStatus())
          .issuer(
            UserDTO
              .builder()
              .id(productOffering.getIssuer().getId().toString())
              .username(productOffering.getIssuer().getUsername())
              .firstname(productOffering.getIssuer().getFirstname())
              .lastname(productOffering.getIssuer().getLastname())
              .country_code(productOffering.getIssuer().getCountry_code())
              .last_seen(productOffering.getIssuer().getLast_seen())
              .address(productOffering.getIssuer().getAddress())
              .organization_name(
                productOffering.getIssuer().getOrganization_name()
              )
              .website(productOffering.getIssuer().getWebsite())
              .build()
          )
          .complianceProfiles(
            productOffering
              .getComplianceProfiles()
              .stream()
              .map(cp ->
                CompilanceProfileDTO
                  .builder()
                  .id(cp.getId())
                  .fileName(cp.getFileName())
                  .url(cp.getUrl())
                  .build()
              )
              .collect(Collectors.toList())
          )
          .issue_date(productOffering.getIssue_date())
          .expiration_date(productOffering.getExpiration_date())
          .request_date(productOffering.getRequest_date())
          .image(productOffering.getImage())
          .request_date(productOffering.getRequest_date())
          .issue_date(productOffering.getIssue_date())
          .expiration_date(productOffering.getExpiration_date())
          .compliances(
            productOffering
              .getCompliances()
              .stream()
              .map(c ->
                ComplianceNamesDTO
                  .builder()
                  .id(c.getId())
                  .complianceName(c.getComplianceName())
                  .build()
              )
              .collect(Collectors.toList())
          )
          .build()
      )
      .collect(Collectors.toList());

    return productOfferings;
  }

  public List<CompilanceProfileDTO> getComplianceProfilesForProductOffering(
    Long productOfferingId
  ) {
    List<ComplianceProfileEntity> complianceProfiles = complianceProfileRepository.findByProductOfferingId(
      productOfferingId
    );
    return complianceProfiles
      .stream()
      .map(complianceProfile ->
        CompilanceProfileDTO
          .builder()
          .id(complianceProfile.getId())
          .fileName(complianceProfile.getFileName())
          .build()
      )
      .collect(Collectors.toList());
  }

  public List<Long> findExpiredProductOfferingIds() {
    return productOfferingRepository.findExpiredProductOfferingIds();
  }

  public List<Long> findWarningProductOfferingIds() {
    return productOfferingRepository.findProductOfferingIdsExpiringSoon();
  }

  public List<Long> checkProductExpiration() {
    logger.info("Executing checkProductExpiration...");
    List<Long> expiredList = findExpiredProductOfferingIds();
    logger.info(
      "Found {} product offering(s) expired: {}",
      expiredList.size(),
      expiredList
    );

    for (Long id : expiredList) {
      ProductOfferingStatesDTO productOfferingStatesDTO = ProductOfferingStatesDTO
        .builder()
        .status(ProductOfferingStates.EXPIRED)
        .build();

      ProductOfferingDTO productOffering = updateStatusProductOffering(
        id,
        productOfferingStatesDTO
      );
      //Send email
      String email = productOffering.email_organization;
      String subject =
        "Compliance of " +
        productOffering.getService_name() +
        " " +
        productOffering.getService_version() +
        " is " +
        productOffering.getStatus();

      try {
        logger.info("Sending expiration warning email to {}", email);
        emailService.sendEmailWithTemplateNoContext(
          email,
          subject,
          "email-expirated-warning"
        );
        productOfferingRepository.updateIsExpirationEmailSent(id);
        logger.info(
          "Expiration email sent flag updated for product offering with id {}",
          id
        );
      } catch (MessagingException e) {
        logger.error(
          "Error sending expiration warning email to {}: {}",
          email,
          e.getMessage()
        );
      }
    }
    logger.info("checkProductExpiration completed successfully.");
    return expiredList;
  }

  public List<Long> checkProductWaringExpiration() {
    logger.info("Executing checkProductWarningExpiration...");
    List<Long> expiredWarningList = findWarningProductOfferingIds();
    logger.info(
      "Found {} product offering(s) nearing expiration: {}",
      expiredWarningList.size(),
      expiredWarningList
    );
    for (Long id : expiredWarningList) {
      ProductOfferingEntity productOffering = getProductOfferingById(id);
      String email = productOffering.getEmail_organization();
      String subject =
        "Compliance of " +
        productOffering.getService_name() +
        " " +
        productOffering.getService_version() +
        " is near to expiration. It will expires on  " +
        productOffering.getExpiration_date();

      try {
        logger.info("Sending expiration warning email to {}", email);
        emailService.sendEmailWithTemplateNoContext(
          email,
          subject,
          "email-2month-warning"
        );
        productOfferingRepository.updateIsExpirationWarningEmailSent(id);
        logger.info(
          "Expiration warning email sent flag updated for product offering with id {}",
          id
        );
      } catch (MessagingException e) {
        logger.error(
          "Error sending expiration warning email to {}: {}",
          email,
          e.getMessage()
        );
      }
    }
    logger.info("checkProductWarningExpiration completed successfully.");
    return expiredWarningList;
  }
}
