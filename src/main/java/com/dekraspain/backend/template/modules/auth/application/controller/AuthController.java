package com.dekraspain.backend.template.modules.auth.application.controller;

import com.dekraspain.backend.template.modules.auth.application.request.LoginRequest;
import com.dekraspain.backend.template.modules.auth.application.request.RegisterRequest;
import com.dekraspain.backend.template.modules.auth.application.response.AuthResponse;
import com.dekraspain.backend.template.modules.auth.domain.service.AuthService;
import com.dekraspain.backend.template.modules.user.domain.service.UserService;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
@CrossOrigin(
  { "http://localhost:4200", "https://xs86qb08-4200.uks1.devtunnels.ms" }
)
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserService userService;

  @Operation(summary = "Login")
  @PostMapping(value = "login")
  public ResponseEntity<AuthResponse> login(
    @Valid @RequestBody LoginRequest request
  ) {
    if (!authService.existsByUsername(request.username)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
    userService.updateLastSeen(request.getUsername());

    return ResponseEntity.ok(authService.login(request));
  }

  @Operation(summary = "Register")
  @PostMapping(value = "register")
  public ResponseEntity<AuthResponse> register(
    @Valid @RequestBody RegisterRequest request
  ) {
    if (authService.existsByUsername(request.username)) {
      return ResponseEntity.badRequest().body(null);
    }

    if (authService.existsByEmail(request.email)) {
      return ResponseEntity.badRequest().body(null);
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
}
