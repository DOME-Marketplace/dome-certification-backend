package com.dekraspain.backend.template.modules.productOffering.domain.service;

import com.dekraspain.backend.template.modules.productOffering.application.request.CompliancesRequest;
import com.dekraspain.backend.template.modules.productOffering.application.request.ProductOfferingRequest;
import com.dekraspain.backend.template.modules.productOffering.domain.model.CompilanceProfileDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ComplianceDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ComplianceStandardsDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStates;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStatesDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.RequestedComplianceLevel;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceProfileEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceProfileRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ProductOfferingRepository;
import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.shared.email.service.EmailService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
  private final ComplianceService complianceService;

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
      .requestedComplianceLevel(
        new RequestedComplianceLevel(request.getRequested_compliances_level())
      )
      .build();

    productOfferingRepository.save(productOffering);

    Path uploadPath = Paths.get(uploadDir);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    for (MultipartFile file : files) {
      String fileHash = calculateFileHash(file);
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
        .url(fileDownloadUri)
        .hash(fileHash)
        .build();

      // Guardar el ComplianceProfileEntity en la base de datos
      complianceProfileRepository.save(complianceProfile);
    }
  }

  private String calculateFileHash(MultipartFile file) throws IOException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] fileBytes = file.getBytes();
      byte[] hashBytes = digest.digest(fileBytes);

      // Convertir el hash a una representación en hexadecimal
      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error al calcular el hash del archivo", e);
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

      Optional<List<CompliancesRequest>> compliances = request.getCompliances();

      for (CompliancesRequest compliance : compliances.get()) {
        Long profileIdLong = compliance.getProfileId();
        Long standardId = compliance.getStandardId();

        complianceService.createCompliance(standardId, profileIdLong, id);
      }
    }
    existingProductOffering.setStatus(request.getStatus());

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
          .email(user.getEmail())
          .firstname(issuer.getFirstname())
          .lastname(issuer.getLastname())
          .organization_country_code(issuer.getCountry_code())
          .last_seen(issuer.getLast_seen())
          .organization_name(issuer.getOrganization_name())
          .organization_id(issuer.getOrganization_id())
          .build();
    }

    UserDTO userDTO = null;
    if (user != null) {
      userDTO =
        UserDTO
          .builder()
          .id(user.getId().toString())
          .email(user.getEmail())
          .username(user.getUsername())
          .firstname(user.getFirstname())
          .lastname(user.getLastname())
          .last_seen(user.getLast_seen())
          .organization_country_code(user.getCountry_code())
          .organization_name(user.getOrganization_name())
          .organization_id(issuer.getOrganization_id())
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
          .hash(cp.getHash())
          .build()
      )
      .collect(Collectors.toList());

    List<ComplianceDTO> compliances = updatedProductOffering
      .getCompliances()
      .stream()
      .map(c ->
        ComplianceDTO
          .builder()
          .id(c.getId())
          .complianceProfile(
            c.getComplianceProfile() != null
              ? CompilanceProfileDTO
                .builder()
                .id(c.getComplianceProfile().getId())
                .fileName(c.getComplianceProfile().getFileName())
                .url(c.getComplianceProfile().getUrl())
                .hash(c.getComplianceProfile().getHash())
                .build()
              : null
          )
          .complianceStandard(
            c.getCompliancesStandard() != null
              ? ComplianceStandardsDTO
                .builder()
                .id(c.getCompliancesStandard().getId())
                .standard(c.getCompliancesStandard().getStandard())
                .description(c.getCompliancesStandard().getDescription())
                .build()
              : null
          )
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
      .requestedComplianceLevel(
        updatedProductOffering.getRequestedComplianceLevel()
      )
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
      .findAllPO()
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
              .organization_country_code(
                productOffering.getIssuer().getCountry_code()
              )
              .organization_id(productOffering.getIssuer().getOrganization_id())
              .last_seen(productOffering.getIssuer().getLast_seen())
              .organization_name(
                productOffering.getIssuer().getOrganization_name()
              )
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
              .organization_country_code(
                productOffering.getUser().getCountry_code()
              )
              .last_seen(productOffering.getUser().getLast_seen())
              .organization_id(productOffering.getUser().getOrganization_id())
              .organization_name(
                productOffering.getUser().getOrganization_name()
              )
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
          .issuer(issuerDTO)
          .user(userDTO)
          .requestedComplianceLevel(
            productOffering.getRequestedComplianceLevel()
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
                  .hash(cp.getHash())
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
                ComplianceDTO
                  .builder()
                  .id(c.getId())
                  .complianceProfile(
                    c.getComplianceProfile() != null
                      ? CompilanceProfileDTO
                        .builder()
                        .id(c.getComplianceProfile().getId())
                        .fileName(c.getComplianceProfile().getFileName())
                        .url(c.getComplianceProfile().getUrl())
                        .hash(c.getComplianceProfile().getHash())
                        .build()
                      : null
                  )
                  .complianceStandard(
                    c.getCompliancesStandard() != null
                      ? ComplianceStandardsDTO
                        .builder()
                        .id(c.getCompliancesStandard().getId())
                        .standard(c.getCompliancesStandard().getStandard())
                        .description(
                          c.getCompliancesStandard().getDescription()
                        )
                        .build()
                      : null
                  )
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
      .findAllByUserId(user.getId())
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
                .organization_country_code(
                  productOffering.getIssuer().getCountry_code()
                )
                .last_seen(productOffering.getIssuer().getLast_seen())
                .organization_name(
                  productOffering.getIssuer().getOrganization_name()
                )
                .organization_id(
                  productOffering.getIssuer().getOrganization_id()
                )
                .build()
              : null
          )
          .user(
            productOffering.getUser() != null
              ? UserDTO
                .builder()
                .id(productOffering.getUser().getId().toString())
                .username(productOffering.getUser().getUsername())
                .firstname(productOffering.getUser().getFirstname())
                .lastname(productOffering.getUser().getLastname())
                .organization_country_code(
                  productOffering.getUser().getCountry_code()
                )
                .last_seen(productOffering.getUser().getLast_seen())
                .organization_name(
                  productOffering.getUser().getOrganization_name()
                )
                .organization_id(productOffering.getUser().getOrganization_id())
                .build()
              : null
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
                  .hash(cp.getHash())
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
                ComplianceDTO
                  .builder()
                  .id(c.getId())
                  .complianceProfile(
                    c.getComplianceProfile() != null
                      ? CompilanceProfileDTO
                        .builder()
                        .id(c.getComplianceProfile().getId())
                        .fileName(c.getComplianceProfile().getFileName())
                        .url(c.getComplianceProfile().getUrl())
                        .hash(c.getComplianceProfile().getHash())
                        .build()
                      : null
                  )
                  .complianceStandard(
                    c.getCompliancesStandard() != null
                      ? ComplianceStandardsDTO
                        .builder()
                        .id(c.getCompliancesStandard().getId())
                        .standard(c.getCompliancesStandard().getStandard())
                        .description(
                          c.getCompliancesStandard().getDescription()
                        )
                        .build()
                      : null
                  )
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
      } catch (Exception e) {
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
      } catch (Exception e) {
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
