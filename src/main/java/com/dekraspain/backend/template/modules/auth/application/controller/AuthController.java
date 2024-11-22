package com.dekraspain.backend.template.modules.auth.application.controller;

import com.dekraspain.backend.template.modules.auth.application.request.LoginRequest;
import com.dekraspain.backend.template.modules.auth.application.request.RegisterRequest;
import com.dekraspain.backend.template.modules.auth.application.request.VerifiableCredentialPayload;
import com.dekraspain.backend.template.modules.auth.application.response.AuthResponse;
import com.dekraspain.backend.template.modules.auth.domain.service.AuthService;
import com.dekraspain.backend.template.spring.Jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;

  @Operation(summary = "Login")
  @PostMapping(value = "login")
  public ResponseEntity<AuthResponse> login(
    @Valid @RequestBody LoginRequest request
  ) {
    if (
      !authService.existsByUsername(request.username) &&
      !authService.existsByEmail(request.username)
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
      authService.existsByUsername(request.username) ||
      authService.existsByEmail(request.username)
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
        .getVerifiableCredential()
        .getCredentialSubject();

      VerifiableCredentialPayload.Mandatee mandatee = verifiableCredential
        .getVerifiableCredential()
        .getCredentialSubject()
        .getMandate()
        .getMandatee();

      // Si el didkey ya existe en la base de datos, proceder con el logins
      if (authService.existsByDidkey(mandatee.getId())) {
        // Login: Obtiene al usuario a partir de didkey
        return ResponseEntity.ok(authService.loginProvider(mandatee.getId()));
      }

      // Si no existe el didkey, intentamos con el email
      if (authService.existsByEmail(mandatee.getEmail())) {
        // Actualiza y asigna el didkey si es necesario

        return ResponseEntity.ok(
          authService.loginProviderAndUpdate(
            mandatee.getEmail(),
            mandatee.getId()
          )
        );
      }

      // Si no se encuentra el usuario, puedes manejar el registro o retornar un error
      return ResponseEntity.ok(authService.registerProvider(credentialSubject));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(null);
    }
  }
}
