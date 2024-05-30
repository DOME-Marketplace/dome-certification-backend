package com.dekraspain.backend.template.modules.productOffering.application.controller;

import com.dekraspain.backend.template.modules.productOffering.application.request.ProductOfferingRequest;
import com.dekraspain.backend.template.modules.productOffering.domain.model.CompilanceProfileDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ComplianceNamesDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStates;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStatesDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.service.ProductOfferingService;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceProfileEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceProfileRepository;
import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.shared.customResponses.ApiResponse;
import com.dekraspain.backend.template.shared.email.service.EmailService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "product-offering")
@RestController
@RequestMapping("/api/v1/product-offering")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
@CrossOrigin(
  { "http://localhost:4200", "https://xs86qb08-4200.uks1.devtunnels.ms" }
)
public class ProductOfferingController {

  private final ProductOfferingService productService;
  private final ComplianceProfileRepository complianceProfileRepository;
  private final EmailService emailService;

  @GetMapping(value = "/")
  public ResponseEntity<List<ProductOfferingDTO>> getAll() {
    List<ProductOfferingDTO> productOfferings = productService.getAllProductOffering();
    return ResponseEntity.ok(productOfferings);
  }

  @GetMapping(value = "/get-by-user-id")
  public ResponseEntity<List<ProductOfferingDTO>> getAllByUserId() {
    List<ProductOfferingDTO> productOfferings = productService.getAllProductOfferingByUserId();
    return ResponseEntity.ok(productOfferings);
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<ProductOfferingDTO> getProductOfferingById(
    @PathVariable Long id
  ) {
    ProductOfferingEntity productOffering = productService.getProductOfferingById(
      id
    );

    if (productOffering == null) {
      return ResponseEntity.notFound().build();
    }

    UserEntity issuer = productOffering.getIssuer();

    List<CompilanceProfileDTO> complianceProfileDTOs = productOffering
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

    List<ComplianceNamesDTO> compliances = productOffering
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
      .address(issuer.getAddress())
      .country_code(issuer.getCountry_code())
      .organization_name(issuer.getOrganization_name())
      .website(issuer.getWebsite())
      .last_seen(issuer.getLast_seen())
      .build();

    ProductOfferingDTO productOfferingDTO = ProductOfferingDTO
      .builder()
      .id(productOffering.getId())
      .service_name(productOffering.getService_name())
      .service_version(productOffering.getService_version())
      .name_organization(productOffering.getName_organization())
      .address_organization(productOffering.getAddress_organization())
      .ISO_Country_Code(productOffering.getISO_Country_Code())
      .url_organization(productOffering.getUrl_organization())
      .email_organization(productOffering.getEmail_organization())
      .id_PO(productOffering.getId_PO())
      .status(productOffering.getStatus())
      .request_date(productOffering.getRequest_date())
      .issue_date(productOffering.getIssue_date())
      .issuer(issuerDTO)
      .expiration_date(productOffering.getExpiration_date())
      .image(productOffering.getImage())
      .complianceProfiles(complianceProfileDTOs)
      .compliances(compliances)
      .build();

    return ResponseEntity.ok().body(productOfferingDTO);
  }

  @PostMapping("/create")
  public ResponseEntity<ApiResponse<Void>> createProductOffering(
    @RequestPart("service_name") String serviceName,
    @RequestPart("service_version") String serviceVersion,
    @RequestPart("name_organization") String nameOrganization,
    @RequestPart("address_organization") String addressOrganization,
    @RequestPart("ISO_Country_Code") String isoCountryCode,
    @RequestPart("url_organization") String urlOrganization,
    @RequestPart("email_organization") String emailOrganization,
    @RequestPart("id_PO") String idPo,
    @RequestPart("files") List<MultipartFile> files
  ) {
    try {
      ProductOfferingRequest request = ProductOfferingRequest
        .builder()
        .service_name(serviceName)
        .service_version(serviceVersion)
        .name_organization(nameOrganization)
        .address_organization(addressOrganization)
        .ISO_Country_Code(isoCountryCode)
        .url_organization(urlOrganization)
        .email_organization(emailOrganization)
        .id_PO(idPo)
        .build();

      productService.createProductOffering(request, files);

      //Send email

      String email = emailOrganization;
      String subject =
        "Compliance of " + serviceName + " " + serviceVersion + " created";

      try {
        emailService.sendEmailWithTemplateNoContext(
          email,
          subject,
          "email-in_progress"
        );
      } catch (MessagingException e) {}

      return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(
          new ApiResponse<>(
            HttpStatus.CREATED.value(),
            "Product offering created",
            null
          )
        );
    } catch (IOException e) {
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
          new ApiResponse<>(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error creating product offering"
          )
        );
    }
  }

  @PostMapping("/status-PO/{id}")
  public ResponseEntity<ProductOfferingDTO> validatePO(
    @PathVariable Long id,
    @RequestBody ProductOfferingStatesDTO status
  ) {
    ProductOfferingDTO productOffering = productService.updateStatusProductOffering(
      id,
      status
    );

    //Send email
    String email = productOffering.email_organization;
    String subject =
      "Compliance of " +
      productOffering.getService_name() +
      " " +
      productOffering.getService_version() +
      " is " +
      status.getStatus();

    if (status.getStatus().equals(ProductOfferingStates.VALIDATED)) {
      try {
        emailService.sendEmailWithTemplateNoContext(
          email,
          subject,
          "email-validated"
        );
      } catch (MessagingException e) {}
    }

    if (status.getStatus().equals(ProductOfferingStates.REJECTED)) {
      try {
        emailService.sendEmailWithTemplateNoContext(
          email,
          subject,
          "email-rejected"
        );
      } catch (MessagingException e) {}
    }

    return ResponseEntity.ok(productOffering);
  }

  @GetMapping("/compliance-profiles/{productOfferingId}")
  public ResponseEntity<List<CompilanceProfileDTO>> getComplianceProfilesForProductOffering(
    @PathVariable Long productOfferingId
  ) {
    System.out.println("productOfferingId: " + productOfferingId);
    List<CompilanceProfileDTO> complianceProfiles = productService.getComplianceProfilesForProductOffering(
      productOfferingId
    );
    return ResponseEntity.ok(complianceProfiles);
  }

  @GetMapping("/download/{id}")
  public ResponseEntity<byte[]> downloadPDF(@PathVariable Long id) {
    // Buscar el archivo en la base de datos
    Optional<ComplianceProfileEntity> fileNameOptional = complianceProfileRepository.findById(
      id
    );
    if (fileNameOptional.isPresent()) {
      ComplianceProfileEntity fileName = fileNameOptional.get();
      // File file = new File(fileName.getUrl());

      // Leer el archivo y convertirlo a bytes
      try {
        Path path = Paths.get(fileName.getUrl());
        byte[] contenido = Files.readAllBytes(path);

        // Configurar los encabezados de la respuesta
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
          fileName.getFileName(),
          fileName.getFileName()
        );
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
      } catch (IOException e) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/test-expiration")
  public ResponseEntity<List<Long>> checkExpirationManually() {
    return ResponseEntity.ok(productService.checkProductExpiration());
  }

  @GetMapping("/test-warning-expiration")
  public ResponseEntity<List<Long>> checkWarningExpirationManually() {
    return ResponseEntity.ok(productService.checkProductWaringExpiration());
  }
}
