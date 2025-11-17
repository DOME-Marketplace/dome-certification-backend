package com.dekraspain.backend.template.modules.productOffering.application.controller;

import com.dekraspain.backend.template.modules.productOffering.application.request.IssuanceRequest;
import com.dekraspain.backend.template.modules.productOffering.application.request.LabelCredentialRequest;
import com.dekraspain.backend.template.modules.productOffering.application.request.ProductOfferingRequest;
import com.dekraspain.backend.template.modules.productOffering.application.response.VerifierTokenResponse;
import com.dekraspain.backend.template.modules.productOffering.domain.model.CompilanceProfileDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ComplianceDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ComplianceStandardsDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.LabelCredentialPayloadDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStates;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStatesDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.service.LabelCredentialService;
import com.dekraspain.backend.template.modules.productOffering.domain.service.ProductOfferingComplianceProfileCriteriaService;
import com.dekraspain.backend.template.modules.productOffering.domain.service.ProductOfferingService;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ComplianceProfileEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceProfileRepository;
import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.shared.customResponses.ApiResponse;
import com.dekraspain.backend.template.shared.email.service.EmailService;
import com.dekraspain.backend.template.spring.Jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "product-offering")
@RestController
@RequestMapping("/api/v1/product-offering")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class ProductOfferingController {

  private final ProductOfferingService productService;
  private final ComplianceProfileRepository complianceProfileRepository;
  private final EmailService emailService;
  private final JwtService jwtService;
  private final RestTemplate restTemplate;
  private final LabelCredentialService labelCredentialService;
  private final ProductOfferingComplianceProfileCriteriaService poCriteriaService;

  @Value("${jwt.oauth.client-id}")
  private String clientId;

  @Value("${jwt.oauth.aud}")
  private String verifierUrl;

  @Value("${jwt.oauth.issuer}")
  private String issuerUrl;

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
          .hash(cp.getHash())
          .build()
      )
      .collect(Collectors.toList());

    List<ComplianceDTO> compliances = productOffering
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

    UserDTO issuerDTO = null;
    if (issuer != null) {
      issuerDTO =
        UserDTO
          .builder()
          .id(issuer.getId().toString())
          .username(issuer.getUsername())
          .firstname(issuer.getFirstname())
          .lastname(issuer.getLastname())
          .email(issuer.getEmail())
          .organization_country_code(issuer.getCountry_code())
          .organization_name(issuer.getOrganization_name())
          .last_seen(issuer.getLast_seen())
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
          .email(productOffering.getUser().getEmail())
          .organization_country_code(
            productOffering.getUser().getCountry_code()
          )
          .organization_name(productOffering.getUser().getOrganization_name())
          .organization_id(productOffering.getUser().getOrganization_id())
          .last_seen(productOffering.getUser().getLast_seen())
          .build();
    }
    // Verificación si issuer es null

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
      .comments(productOffering.getComments())
      .VAT_ID(productOffering.getVAT_ID())
      .issuer(issuerDTO) // Puede ser null sin causar error
      .user(userDTO) // Puede ser null sin causar error
      .expiration_date(productOffering.getExpiration_date())
      .image(productOffering.getImage())
      .complianceProfiles(complianceProfileDTOs)
      .compliances(compliances)
      .requestedComplianceLevel(productOffering.getRequestedComplianceLevel())
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
    @RequestPart("VAT_ID") String vatId,
    @RequestPart("id_PO") String idPo,
    @RequestPart("requested_compliance_level") String compliancesLevel,
    @RequestPart("files") List<MultipartFile> files
  ) {
    try {
      Authentication authentication = SecurityContextHolder
        .getContext()
        .getAuthentication();

      UserEntity user = (UserEntity) authentication.getPrincipal();
      ProductOfferingRequest request = ProductOfferingRequest
        .builder()
        .service_name(serviceName)
        .service_version(serviceVersion)
        .name_organization(nameOrganization)
        .address_organization(addressOrganization)
        .ISO_Country_Code(isoCountryCode)
        .url_organization(urlOrganization)
        .email_organization(emailOrganization)
        .VAT_ID(vatId)
        .id_PO(idPo)
        .requested_compliances_level(compliancesLevel)
        .build();

      productService.createProductOffering(request, files);
      // Send email
      String email = user.getEmail();
      String subject =
        "Compliance of " + serviceName + " " + serviceVersion + " created";

      try {
        emailService.sendEmailWithTemplateNoContext(
          email,
          subject,
          "email-in_progress"
        );
      } catch (Exception e) {
        // Log the error but don't propagate it
        log.warn("Error sending email to: {}", email, e);
      }

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
    } catch (Exception e) {
      // Catch any other unexpected exceptions and log them
      log.error("Unexpected error occurred", e);
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
          new ApiResponse<>(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Unexpected error"
          )
        );
    }
  }

  @PostMapping("/certificate")
  public ResponseEntity<ApiResponse<Void>> createProductOfferingM2M(
    @RequestPart("product_specification_id") String idPo,
    @RequestPart("service_name") String serviceName,
    @RequestPart("service_version") String serviceVersion,
    @RequestPart("organization_name") String nameOrganization,
    @RequestPart("organization_address") String addressOrganization,
    @RequestPart("organization_country") String isoCountryCode,
    @RequestPart("organization_email") String emailOrganization,
    @RequestPart("organization_url") String urlOrganization,
    @RequestPart("organization_vat_id") String vatId,
    @RequestPart("requested_compliance_level") String compliancesLevel,
    @RequestPart("files") List<MultipartFile> files
  ) {
    try {
      // Log entry and current principal to help identify authentication path (M2M vs user JWT)
      Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
      if (currentAuth != null) {
        log.info("certificate called - current authentication principal: {} authorities: {}", currentAuth.getPrincipal(), currentAuth.getAuthorities());
      } else {
        log.info("certificate called - no authentication in SecurityContext");
      }
//  
      ProductOfferingRequest request = ProductOfferingRequest
        .builder()
        .service_name(serviceName)
        .service_version(serviceVersion)
        .name_organization(nameOrganization)
        .address_organization(addressOrganization)
        .ISO_Country_Code(isoCountryCode)
        .url_organization(urlOrganization)
        .email_organization(emailOrganization)
        .VAT_ID(vatId)
        .id_PO(idPo)
        .requested_compliances_level(compliancesLevel)
        .build();

      productService.createProductOfferingM2M(request, files);
      
      String emailSubject =
      "Compliance of " + serviceName + " " + serviceVersion + " created";
      

      try {
        emailService.sendEmailWithTemplateNoContext(
          emailOrganization,
          emailSubject,
          "email-in_progress"
        );
      } catch (Exception e) {
        // Log the error but don't propagate it
        log.warn("Error sending email to: {}", emailOrganization, e);
      }

      return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(
          new ApiResponse<>(
            HttpStatus.CREATED.value(),
            null
          )
        );
    } catch (IOException e) {
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
          new ApiResponse<>(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            null
          )
        );
    } catch (Exception e) {
      // Catch any other unexpected exceptions and log them
      log.error("Unexpected error occurred", e);
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
          new ApiResponse<>(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Unexpected error"
          )
        );
    }
  }

  @PostMapping("/status-PO/{id}")
  public ResponseEntity<ProductOfferingDTO> validatePO(
    @PathVariable Long id,
    @RequestBody ProductOfferingStatesDTO request
  ) {
    ProductOfferingDTO productOffering = productService.updateStatusProductOffering(
      id,
      request
    );

    // Send email
    String email = productOffering.getUser().getEmail();
    String subject = String.format(
      "Compliance of %s %s is %s",
      productOffering.getService_name(),
      productOffering.getService_version(),
      request.getStatus()
    );

    // Enviar correo si el estado es VALIDADO
    if (request.getStatus().equals(ProductOfferingStates.VALIDATED)) {
      try {
        emailService.sendEmailWithTemplateNoContext(
          email,
          subject,
          "email-validated"
        );
      } catch (Exception e) {
        // Log the error but don't propagate it
        log.warn("Error sending email for validated status to: {}", email, e);
      }
    }

    // Enviar correo si el estado es RECHAZADO
    if (request.getStatus().equals(ProductOfferingStates.REJECTED)) {
      try {
        emailService.sendEmailWithTemplateNoContext(
          email,
          subject,
          "email-rejected"
        );
      } catch (Exception e) {
        // Log the error but don't propagate it
        log.warn("Error sending email for rejected status to: {}", email, e);
      }
    }

    return ResponseEntity.ok(productOffering);
  }

  @PostMapping("/resend-email/{id}")
  public ResponseEntity<String> resendEmail(@PathVariable Long id) {
    try {
      // Obtener la entidad ProductOffering mediante el id
      ProductOfferingEntity productOffering = productService.getProductOfferingById(
        id
      );

      if (productOffering == null) {
        return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body("Product Offering not found");
      }

      // Datos para el correo
      String email = productOffering.getUser().getEmail();
      String subject = String.format(
        "Compliance of %s %s",
        productOffering.getService_name(),
        productOffering.getService_version()
      );

      try {
        if (
          productOffering.getStatus().equals(ProductOfferingStates.REJECTED)
        ) {
          emailService.sendEmailWithTemplateNoContext(
            email,
            subject,
            "email-rejected"
          );
        }
        if (
          productOffering.getStatus().equals(ProductOfferingStates.VALIDATED)
        ) {
          emailService.sendEmailWithTemplateNoContext(
            email,
            subject,
            "email-validated"
          );
        }
        log.info("Email sent to: {}", email);
      } catch (Exception e) {
        log.warn("Error sending email to: {}", email, e);
        return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error sending email");
      }

      return ResponseEntity.ok("Email resent successfully");
    } catch (Exception e) {
      log.error("Error processing email resend", e);
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error processing email resend");
    }
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

  @PostMapping("/generate-label-credential")
  public ResponseEntity<?> generateLabelCredential(
    @RequestBody LabelCredentialRequest request
  ) {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();
    UserEntity user = (UserEntity) authentication.getPrincipal();

    if (user == null) {
      return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body("User not authenticated");
    }

    try {
      LabelCredentialPayloadDTO labelCredentialPayload = labelCredentialService.generateLabelCredentialPayload(
        request.getPoId(),
        request.getPayload(),
        user,
        request.getValidUntil()
      );
      ProductOfferingEntity productOffering = productService.getProductOfferingById(
        request.getPoId()
      );
      String client_assertion = jwtService.generateClientAssertionTokenM2M();
      HttpHeaders tokenHeaders = new HttpHeaders();
      tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> verifierTokenBody = new LinkedMultiValueMap<>();
      verifierTokenBody.add("client_id", clientId);
      verifierTokenBody.add("grant_type", "client_credentials");
      verifierTokenBody.add(
        "client_assertion_type",
        "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
      );
      verifierTokenBody.add("client_assertion", client_assertion);

      HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(
        verifierTokenBody,
        tokenHeaders
      );

      ResponseEntity<VerifierTokenResponse> tokenResponse = restTemplate.postForEntity(
        verifierUrl + "/oidc/token",
        tokenRequest,
        VerifierTokenResponse.class
      );

      VerifierTokenResponse tokenBody = tokenResponse.getBody();
      if (tokenBody == null || tokenBody.getAccess_token() == null) {
        log.info("Missing access_token in verifier response");
        return ResponseEntity
          .status(HttpStatus.BAD_GATEWAY)
          .body("Missing access token");
      }

      String accessToken = tokenBody.getAccess_token();
      log.info("M2M Token: {}", accessToken);

      // 1. Enviar al issuer
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      headers.set("X-ID-TOKEN", request.getIdToken());
      headers.setContentType(MediaType.APPLICATION_JSON);

      // Validar que el usuario no sea null
      if (productOffering.getUser() == null) {
        log.error("ProductOffering {} has no associated user", productOffering.getId());
        return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body("ProductOffering has no associated user");
      }

      String ownerEmail = productOffering.getUser().getEmail();
      log.info("Product Offering User Email: {}", ownerEmail);
      
      Map<String, Object> body = Map.of(
        "schema",
        "gx:LabelCredential",
        "operation_mode",
        "S",
        "format",
        "jwt_vc_json",
        "payload",
        labelCredentialPayload,
        "credential_owner_email",
        ownerEmail,
        "response_uri",
        request.getResponse_uri()
      );

      // Mostrar el payload en formato JSON
      try {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(body);
        log.info("Generated body for issuer (JSON):\n{}", json);
      } catch (Exception e) {
        log.warn("No se pudo serializar el payload a JSON", e);
      }

      // print accessToken
      // log.info("Access Token: {}", accessToken);

      HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(
        body,
        headers
      );

      ResponseEntity<String> issuerResponse = restTemplate.postForEntity(
        issuerUrl + "/issuer-api/vci/v1/issuances/external",
        httpEntity,
        String.class
      );

      log.info(
        "Issuer responded with status: {}",
        issuerResponse.getStatusCode()
      );
      log.info("Issuer response body: {}", issuerResponse.getBody());

      // Si la respuesta no es 2xx, propagamos el error
      if (!issuerResponse.getStatusCode().is2xxSuccessful()) {
        return ResponseEntity
          .status(issuerResponse.getStatusCode())
          .body(issuerResponse.getBody());
      }

      // 2. Actualizar PO si hay `poId` y `data`
      if (request.getPoId() != null && request.getData() != null) {
        productService.updateStatusProductOffering(
          request.getPoId(),
          request.getData()
        );
      }
      // Guardar los criterios validados usando el servicio
      if (request.getPayload() != null) {
        poCriteriaService.saveValidatedCriteria(
          productService.getProductOfferingById(request.getPoId()),
          request.getPayload(),
          user
        );
      }

      return ResponseEntity
        .status(issuerResponse.getStatusCode())
        .body(issuerResponse.getBody());
    } catch (HttpClientErrorException ex) {
      log.info("HTTP error from external service", ex);
      return ResponseEntity
        .status(ex.getStatusCode())
        .body(ex.getResponseBodyAsString());
    } catch (RestClientException e) {
      log.info("Error issuing certificate", e);
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error issuing certificate");
    }
  }

  @PostMapping("/issuances")
  public ResponseEntity<?> issueCertificate(
    @RequestBody IssuanceRequest request
  ) {
    try {
      String client_assertion = jwtService.generateClientAssertionTokenM2M();
      HttpHeaders tokenHeaders = new HttpHeaders();
      tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> verifierTokenBody = new LinkedMultiValueMap<>();
      verifierTokenBody.add("client_id", clientId);
      verifierTokenBody.add("grant_type", "client_credentials");
      verifierTokenBody.add(
        "client_assertion_type",
        "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
      );
      verifierTokenBody.add("client_assertion", client_assertion);

      HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(
        verifierTokenBody,
        tokenHeaders
      );

      ResponseEntity<VerifierTokenResponse> tokenResponse = restTemplate.postForEntity(
        verifierUrl + "/oidc/token",
        tokenRequest,
        VerifierTokenResponse.class
      );

      VerifierTokenResponse tokenBody = tokenResponse.getBody();
      if (tokenBody == null || tokenBody.getAccess_token() == null) {
        log.info("Missing access_token in verifier response");
        return ResponseEntity
          .status(HttpStatus.BAD_GATEWAY)
          .body("Missing access token");
      }

      String accessToken = tokenBody.getAccess_token();

      // 1. Enviar al issuer
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      headers.set("X-ID-TOKEN", request.getIdToken());
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(
        request.getPayload(),
        headers
      );

      ResponseEntity<String> issuerResponse = restTemplate.postForEntity(
        issuerUrl + "/issuer-api/vci/v1/issuances/external",
        httpEntity,
        String.class
      );

      log.info(
        "Issuer responded with status: {}",
        issuerResponse.getStatusCode()
      );
      log.info("Issuer response body: {}", issuerResponse.getBody());

      // Si la respuesta no es 2xx, propagamos el error
      if (!issuerResponse.getStatusCode().is2xxSuccessful()) {
        return ResponseEntity
          .status(issuerResponse.getStatusCode())
          .body(issuerResponse.getBody());
      }

      // 2. Actualizar PO si hay `poId` y `data`
      if (request.getPoId() != null && request.getData() != null) {
        productService.updateStatusProductOffering(
          request.getPoId(),
          request.getData()
        );
      }

      return ResponseEntity
        .status(issuerResponse.getStatusCode())
        .body(issuerResponse.getBody());
    } catch (HttpClientErrorException ex) {
      log.info("HTTP error from external service", ex);
      return ResponseEntity
        .status(ex.getStatusCode())
        .body(ex.getResponseBodyAsString());
    } catch (RestClientException e) {
      log.info("Error issuing certificate", e);
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error issuing certificate");
    }
  }
}

