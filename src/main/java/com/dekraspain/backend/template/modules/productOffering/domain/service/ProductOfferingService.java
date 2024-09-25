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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
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
      .user(user)
      .status(ProductOfferingStates.IN_PROGRESS)
      .VAT_ID(request.getVAT_ID())
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
    ProductOfferingStatesDTO request
  ) {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();

    ProductOfferingEntity existingProductOffering = getProductOfferingById(id);
    UserEntity user = (UserEntity) authentication.getPrincipal();
    existingProductOffering.setStatus(request.getStatus());

    if (request.getStatus() == ProductOfferingStates.REJECTED) {
      existingProductOffering.setComments(request.getComments());
      existingProductOffering.setIssue_date(new Date());
    }

    if (
      request.getStatus() == ProductOfferingStates.VALIDATED &&
      request.getExpiration_date() != null
    ) {
      existingProductOffering.setIssue_date(new Date());
      existingProductOffering.setIssuer(user);
      existingProductOffering.setExpiration_date(request.getExpiration_date());

      Optional<List<String>> compliances = request.getCompliances();

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

    // Verificación si issuer es null
    UserEntity issuer = updatedProductOffering.getIssuer();
    UserDTO issuerDTO = null;
    if (issuer != null) {
      issuerDTO =
        UserDTO
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
    }

    UserDTO userDTO = null;
    if (user != null) {
      userDTO =
        UserDTO
          .builder()
          .id(user.getId().toString())
          .username(user.getUsername())
          .firstname(user.getFirstname())
          .lastname(user.getLastname())
          .country_code(user.getCountry_code())
          .last_seen(user.getLast_seen())
          .address(user.getAddress())
          .organization_name(user.getOrganization_name())
          .website(user.getWebsite())
          .build();
    }

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
      .user(userDTO)
      .issuer(issuerDTO) // Puede ser null sin causar error
      .comments(updatedProductOffering.getComments())
      .VAT_ID(updatedProductOffering.getVAT_ID())
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
      .findAll(Sort.by(Sort.Direction.DESC, "request_date"))
      .stream()
      .map(productOffering -> {
        // Verificación si issuer es null
        UserDTO issuerDTO = null;
        if (productOffering.getIssuer() != null) {
          issuerDTO =
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
              .build();
        }
        UserDTO userDTO = null;
        if (productOffering.getUser() != null) {
          userDTO =
            UserDTO
              .builder()
              .id(productOffering.getUser().getId().toString())
              .username(productOffering.getUser().getUsername())
              .firstname(productOffering.getUser().getFirstname())
              .lastname(productOffering.getUser().getLastname())
              .country_code(productOffering.getUser().getCountry_code())
              .last_seen(productOffering.getUser().getLast_seen())
              .address(productOffering.getUser().getAddress())
              .organization_name(
                productOffering.getUser().getOrganization_name()
              )
              .website(productOffering.getUser().getWebsite())
              .build();
        }

        return ProductOfferingDTO
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
          .VAT_ID(productOffering.getVAT_ID())
          .comments(productOffering.getComments())
          .status(productOffering.getStatus())
          .issuer(issuerDTO) // Puede ser null sin causar error
          .user(userDTO)
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
          .build();
      })
      .collect(Collectors.toList());

    return productOfferings;
  }

  public List<ProductOfferingDTO> getAllProductOfferingByUserId() {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();

    UserEntity user = (UserEntity) authentication.getPrincipal();
    List<ProductOfferingDTO> productOfferings = productOfferingRepository
      .findAllByUserId(user.id)
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
          .VAT_ID(productOffering.getVAT_ID())
          .comments(productOffering.getComments())
          .status(productOffering.getStatus())
          .issuer(
            productOffering.getIssuer() != null
              ? UserDTO
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
              : null // Si el issuer es null, se asigna null
          )
          .user(
            productOffering.getUser() != null
              ? UserDTO
                .builder()
                .id(productOffering.getUser().getId().toString())
                .username(productOffering.getUser().getUsername())
                .firstname(productOffering.getUser().getFirstname())
                .lastname(productOffering.getUser().getLastname())
                .country_code(productOffering.getUser().getCountry_code())
                .last_seen(productOffering.getUser().getLast_seen())
                .address(productOffering.getUser().getAddress())
                .organization_name(
                  productOffering.getUser().getOrganization_name()
                )
                .website(productOffering.getUser().getWebsite())
                .build()
              : null // Si el user es null, se asigna null
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
