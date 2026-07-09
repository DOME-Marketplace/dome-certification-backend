package com.dekraspain.backend.template.modules.auth.application.controller;

import com.dekraspain.backend.template.modules.auth.application.request.VerifiableCredentialPayload;
import com.dekraspain.backend.template.modules.auth.application.response.AuthResponse;
import com.dekraspain.backend.template.modules.auth.domain.service.AuthService;
import com.dekraspain.backend.template.modules.user.domain.model.UserRole;
import com.dekraspain.backend.template.modules.user.domain.service.UserService;
import com.dekraspain.backend.template.spring.Jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserService userService;
  private final JwtService jwtService;

  @Operation(summary = "oauth-token")
  @GetMapping(value = "oauth-token")
  public String generateRequestToken() {
    return jwtService.generateRequestToken();
  }

  @Operation(summary = "client-assertion-token")
  @GetMapping(value = "client-assertion-token")
  public String generateClientAssertionToken() {
    return jwtService.generateClientAssertionToken();
  }

  @Operation(summary = "client-assertion-token-m2m")
  @GetMapping(value = "client-assertion-token-m2m")
  public String generateClientAssertionTokenM2M() {
    return jwtService.generateClientAssertionTokenM2M();
  }

  @Operation(summary = "exchange-token")
  @PostMapping(value = "exchange-token")
  public ResponseEntity<AuthResponse> exchangeToken(
    @RequestBody String requesToken
  ) {
    try {
      Map<String, Object> payload = jwtService.verifyJwt(requesToken);

      VerifiableCredentialPayload verifiableCredential = jwtService.parseJwtPayload(
        payload
      );
      // Print the verifiable credential as JSON for debugging
      try {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String verifiableJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(verifiableCredential);
        System.out.println(verifiableJson);
      } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
        // If serialization fails, log the error to stdout for debugging
        e.printStackTrace();
      }
      VerifiableCredentialPayload.CredentialSubject credentialSubject = verifiableCredential
        .getVc()
        .getCredentialSubject();

      VerifiableCredentialPayload.Mandate mandate = credentialSubject.getMandate();

      VerifiableCredentialPayload.Mandatee mandatee = credentialSubject
        .getMandate()
         .getMandatee();

      VerifiableCredentialPayload.Mandator mandator = credentialSubject
        .getMandate()
        .getMandator();

      // Validar que los datos requeridos no sean nulos
      if (credentialSubject == null || mandate == null) {
        throw new IllegalArgumentException("Mandate data is required");
      }

      List<VerifiableCredentialPayload.Power> power = mandate.getPower();

      // Determinar el rol del usuario
      UserRole role = (
          power != null &&
          power
            .stream()
            .anyMatch(p -> "certification".equalsIgnoreCase(p.getTmf_function())
            )
        )
        ? UserRole.EMPLOYEE
        : UserRole.CUSTOMER;

      // El id del mandatee es opcional: las cuentas nuevas no lo traen.
      // Sólo se valida por didkey cuando la credencial incluye el mandatee.id;
      // en caso contrario se identifica al usuario por el email de la credencial.
      String rawDidkey = mandatee.getId();
      String didkey = (rawDidkey != null && !rawDidkey.isBlank())
        ? rawDidkey
        : null;

      // Si el didkey existe, proceder con el login
      if (didkey != null && userService.existsByDidkey(didkey)) {
        return ResponseEntity.ok(
          authService.loginProvider(
            didkey,
            role,
            mandator.getOrganizationIdentifier(),
            mandator.getEmail()
          )
        );
      }

      // Si no existe el didkey, intentamos con el email
      if (userService.existsByEmail(mandatee.getEmail())) {
        return ResponseEntity.ok(
          authService.loginProviderAndUpdate(
            mandatee.getEmail(),
            didkey,
            role,
            mandator.getOrganizationIdentifier(),
            mandator.getEmail()
          )
        );
      }

      // Si no se encuentra el usuario, registrar
      return ResponseEntity.ok(
        authService.registerProvider(credentialSubject, role)
      );
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(null);
    }
  }
}
