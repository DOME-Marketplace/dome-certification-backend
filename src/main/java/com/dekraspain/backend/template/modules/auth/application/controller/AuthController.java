package com.dekraspain.backend.template.modules.auth.application.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dekraspain.backend.template.modules.auth.application.request.LoginRequest;
import com.dekraspain.backend.template.modules.auth.application.request.RegisterRequest;
import com.dekraspain.backend.template.modules.auth.application.request.VerifiableCredentialPayload;
import com.dekraspain.backend.template.modules.auth.application.response.AuthResponse;
import com.dekraspain.backend.template.modules.auth.domain.service.AuthService;
import com.dekraspain.backend.template.modules.user.domain.model.UserRole;
import com.dekraspain.backend.template.modules.user.domain.service.UserService;
import com.dekraspain.backend.template.spring.Jwt.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserService userService;
  private final JwtService jwtService;

  @Operation(summary = "Login")
  @PostMapping(value = "login")
  public ResponseEntity<AuthResponse> login(
    @Valid @RequestBody LoginRequest request
  ) {
    if (
      !userService.existsByUsername(request.username) &&
      !userService.existsByEmail(request.username)
    ) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    return ResponseEntity.ok(authService.login(request));
  }

  @Operation(summary = "Register")
  @PostMapping(value = "register")
  public ResponseEntity<AuthResponse> register(
    @Valid @RequestBody RegisterRequest request
  ) {
    if (
      userService.existsByUsername(request.username) ||
      userService.existsByEmail(request.username)
    ) {
      return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(null);
    }

    return ResponseEntity.ok(authService.register(request));
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> handleValidationExceptions(
    MethodArgumentNotValidException ex
  ) {
    Map<String, String> errors = new HashMap<>();

    ex
      .getBindingResult()
      .getAllErrors()
      .forEach(error -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
      });

    return errors;
  }

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

      VerifiableCredentialPayload.CredentialSubject credentialSubject = verifiableCredential
        .getVc()
        .getCredentialSubject();

      VerifiableCredentialPayload.Mandatee mandatee = credentialSubject
        .getMandate()
        .getMandatee();

      // Validar que los datos requeridos no sean nulos
      if (credentialSubject == null || credentialSubject.getMandate() == null) {
        throw new IllegalArgumentException("Mandate data is required");
      }

      List<VerifiableCredentialPayload.Power> power = credentialSubject
        .getMandate()
        .getPower();

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

      // Si el didkey ya existe, proceder con el logins
      if (userService.existsByDidkey(mandatee.getId())) {
        return ResponseEntity.ok(
          authService.loginProvider(mandatee.getId(), role)
        );
      }

      // Si no existe el didkey, intentamos con el email
      if (userService.existsByEmail(mandatee.getEmail())) {
        return ResponseEntity.ok(
          authService.loginProviderAndUpdate(
            mandatee.getEmail(),
            mandatee.getId(),
            role
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
