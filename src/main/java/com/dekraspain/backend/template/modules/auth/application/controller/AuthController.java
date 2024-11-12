package com.dekraspain.backend.template.modules.auth.application.controller;

import com.dekraspain.backend.template.modules.auth.application.request.LoginRequest;
import com.dekraspain.backend.template.modules.auth.application.request.RegisterRequest;
import com.dekraspain.backend.template.modules.auth.application.response.AuthResponse;
import com.dekraspain.backend.template.modules.auth.domain.service.AuthService;
import com.dekraspain.backend.template.modules.user.domain.service.UserService;
import com.dekraspain.backend.template.spring.Jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
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
import org.springframework.web.client.RestTemplate;

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

  @Operation(summary = "oauth-token")
  @GetMapping(value = "oauth-token")
  public String generateOauthToken() {
    return jwtService.generateOauthToken();
  }

  @PostMapping("/verify")
  public ResponseEntity<Map<String, Object>> verifyJwt(
    @RequestBody Map<String, String> requestBody
  ) throws JwtException {
    try {
      // Paso 1: Extraer el JWT desde la solicitud
      String jwt = requestBody.get("jwt");
      if (jwt == null || jwt.isEmpty()) {
        return ResponseEntity
          .badRequest()
          .body(Map.of("error", "JWT is missing"));
      }

      // Paso 2: Extraer el DID del JWT (campo "aud")
      Map<String, Object> payload = jwtService.decodeJwt(jwt);

      String didUrl =
        "https://verifier.dome-marketplace-sbx.org/oidc/did/" +
        payload.get("iss");

      // Paso 3: Obtener la clave pública desde la URL DID
      PublicKey publicKey = getPublicKeyFromServer(didUrl);

      // Paso 4: Verificar la firma del JWT con la clave pública
      Jws<Claims> claimsJws = Jwts
        .parserBuilder()
        .setSigningKey(publicKey)
        .build()
        .parseClaimsJws(jwt); // Verifica la firma y lanza un error si no es válida

      // Convertir Claims a un Map para el JSON de respuesta
      Claims claims = claimsJws.getBody();

      // Convertir Claims a un Map para el JSON de respuesta
      Map<String, Object> claimsMap = new HashMap<>(claims);

      return ResponseEntity.ok(claimsMap);
    } catch (Exception e) {
      // Cambiar la respuesta de error a un Map<String, Object> para mantener la consistencia
      return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", "Invalid JWT: " + e.getMessage()));
    }
  }

  private PublicKey getPublicKeyFromServer(String didUrl) throws Exception {
    // Realizamos una solicitud HTTP GET a la URL DID para obtener la clave pública
    RestTemplate restTemplate = new RestTemplate();
    String jsonResponse = restTemplate.getForObject(didUrl, String.class);

    // Extraemos las coordenadas x e y de la respuesta JSON
    String x = "AafPhf-qT_gPc7yvtCd6jSAGUedUalQAFRQBv3fuaUM";
    String y = "modVruGC6Le8PiD2Bi5vTsPJFoytARsMYTac56XJwz0";
    // String x = extractJsonValue(jsonResponse, "x");
    // String y = extractJsonValue(jsonResponse, "y");

    // Decodificar las coordenadas x e y desde Base64
    byte[] xBytes = Base64.getUrlDecoder().decode(x);
    byte[] yBytes = Base64.getUrlDecoder().decode(y);

    // Crear el punto EC con las coordenadas x e y
    ECPoint ecPoint = new ECPoint(
      new java.math.BigInteger(1, xBytes),
      new java.math.BigInteger(1, yBytes)
    );

    // Crear la clave pública EC
    ECParameterSpec ecParams = getECParameterSpec();
    ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecPoint, ecParams);
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(
      publicKeySpec
    );

    return publicKey;
  }

  private String extractJsonValue(String json, String key) {
    int startIndex = json.indexOf(key + "\":\"") + (key.length() + 3);
    int endIndex = json.indexOf("\"", startIndex);
    return json.substring(startIndex, endIndex);
  }

  private ECParameterSpec getECParameterSpec()
    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
    keyPairGenerator.initialize(ecSpec);
    ECParameterSpec ecParams =
      ((ECPublicKey) keyPairGenerator.genKeyPair().getPublic()).getParams();
    return ecParams;
  }
}
