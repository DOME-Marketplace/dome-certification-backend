package com.dekraspain.backend.template.spring.Jwt;

import com.dekraspain.backend.template.modules.auth.application.request.KeysContainer;
import com.dekraspain.backend.template.modules.auth.application.request.VerifiableCredentialPayload;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class JwtService {

  @Value("${jwt.secret.key}")
  private String secretKey;

  @Value("${jwt.private.key.d}")
  private String d;

  @Value("${jwt.oauth.client-id}")
  private String clientId;

  @Value("${jwt.oauth.redirect-uri}")
  private String redirectUri;

  @Value("${jwt.oauth.aud}")
  private String aud;

  @Value("${jwt.lear.credential}")
  private String learCredentialJwt;

  private final long expirationTime = 86400000;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public String getToken(UserEntity user) {
    Map<String, Object> extraClaims = new HashMap<>();
    // Aquí ya no necesitas mapear a un UserDTO, directamente trabajas con UserEntity
    return getToken(extraClaims, user);
  }

  public VerifiableCredentialPayload parseJwtPayload(
    Map<String, Object> payload
  ) {
    try {
      // Convertir el payload Map a un objeto AuthRequest automáticamente
      return objectMapper.convertValue(
        payload,
        VerifiableCredentialPayload.class
      );
    } catch (IllegalArgumentException e) {
      log.error("Error parsing JWT payload", e);
      throw new RuntimeException("Error parsing JWT payload", e);
    }
  }

  private String getToken(Map<String, Object> extraClaims, UserEntity user) {
    Calendar iat = Calendar.getInstance();
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.HOUR, 12);
    extraClaims.put("role", user.getRole());
    return Jwts
      .builder()
      .setClaims(extraClaims)
      .setSubject(user.getId().toString())
      .setIssuedAt(iat.getTime())
      .setExpiration(exp.getTime())
      .signWith(getKey(), SignatureAlgorithm.HS256)
      .compact();
  }

  // Nuevo método para generar un token que incluya parámetros adicionales
  public String generateRequestToken() {
    try {
      Claims claims = Jwts.claims();
      claims.put("iss", clientId);
      claims.put("aud", aud);
      claims.put("response_type", "code");
      claims.put("client_id", clientId);
      claims.put("redirect_uri", redirectUri);
      claims.put("scope", "openid learcredential");

      // Crear los parámetros del header
      Map<String, Object> headerParams = new HashMap<>();
      headerParams.put("typ", "JWT");
      headerParams.put("kid", clientId);

      return Jwts
        .builder()
        .setHeaderParams(headerParams)
        .setClaims(claims)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .signWith(loadPrivateKey(), SignatureAlgorithm.ES256)
        .compact();
    } catch (Exception e) {
      throw new RuntimeException("Error generating OAuth token", e);
    }
  }

  public String generateClientAssertionToken() {
    try {
      Claims claims = Jwts.claims();
      claims.put("iss", clientId);

      //supuestamente es el did del empleado de dentro de la
      claims.put("sub", clientId);
      claims.put("aud", aud);
      claims.put("jti", UUID.randomUUID().toString());

      // Crear los parámetros del header
      Map<String, Object> headerParams = new HashMap<>();
      headerParams.put("typ", "JWT");
      headerParams.put("kid", clientId);

      return Jwts
        .builder()
        .setHeaderParams(headerParams)
        .setClaims(claims)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(loadPrivateKey(), SignatureAlgorithm.ES256)
        .compact();
    } catch (Exception e) {
      throw new RuntimeException("Error generating OAuth token", e);
    }
  }

  public String generateClientAssertionTokenM2M() {
    try {
      String vpJwt = generateVerifiablePresentationToken();
      String vpTokenBase64 = Base64
        .getEncoder()
        .encodeToString(vpJwt.getBytes(StandardCharsets.UTF_8));

      Claims claims = Jwts.claims();
      claims.put("iss", clientId);
      claims.put("sub", clientId);
      claims.put("aud", aud);
      claims.put("jti", UUID.randomUUID().toString());
      claims.put("iat", System.currentTimeMillis() / 1000);
      claims.put("exp", (System.currentTimeMillis() + expirationTime) / 1000);
      claims.put("vp_token", vpTokenBase64); // ✅ Base64 del JWT firmado

      Map<String, Object> headerParams = new HashMap<>();
      headerParams.put("typ", "JWT");
      headerParams.put("kid", clientId);

      return Jwts
        .builder()
        .setHeaderParams(headerParams)
        .setClaims(claims)
        .signWith(loadPrivateKey(), SignatureAlgorithm.ES256)
        .compact();
    } catch (Exception e) {
      throw new RuntimeException("Error generating client assertion token", e);
    }
  }

  public String generateVerifiablePresentationToken() {
    try {
      if (learCredentialJwt == null || learCredentialJwt.isEmpty()) {
        throw new IllegalStateException(
          "LEAR_CREDENTIAL_JWT env variable is not set"
        );
      }

      long nowSeconds = System.currentTimeMillis() / 1000;

      Map<String, Object> vpClaim = new HashMap<>();
      vpClaim.put("type", List.of("VerifiablePresentation"));
      vpClaim.put("verifiableCredential", List.of(learCredentialJwt)); // JWT tal cual

      Map<String, Object> claims = new HashMap<>();
      claims.put("vp", vpClaim);
      claims.put("iss", clientId);
      claims.put("jti", UUID.randomUUID().toString());
      claims.put("iat", nowSeconds);
      claims.put("nbf", nowSeconds);
      claims.put("exp", nowSeconds + 30);

      return Jwts
        .builder()
        .setHeaderParam("typ", "JWT")
        .setClaims(claims)
        .signWith(loadPrivateKey(), SignatureAlgorithm.ES256)
        .compact();
    } catch (Exception e) {
      throw new RuntimeException("Error generating VP JWT", e);
    }
  }

  private Key getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public ECPrivateKey loadPrivateKey() throws Exception {
    // Decodificar las coordenadas de Base64
    byte[] dBytes = Base64.getUrlDecoder().decode(d);

    // Crear la clave privada EC
    ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(
      new java.math.BigInteger(1, dBytes),
      getECParameterSpec()
    );
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(
      privateKeySpec
    );

    return privateKey;
  }

  public String getUserIdFromToken(String token) {
    return getClaim(token, Claims::getSubject);
  }

  public boolean isTokenValid(String token, UserEntity user) {
    final String userId = getUserIdFromToken(token);
    return userId.equals(user.getId().toString()) && !isTokenExpired(token);
  }

  private Claims getAllClaims(String token) {
    return Jwts
      .parserBuilder()
      .setSigningKey(getKey())
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  // Método para decodificar un JWT sin verificar la firma
  @SuppressWarnings("unchecked")
  public Map<String, Object> decodeJwt(String jwt) {
    try {
      // Divide el JWT en las tres partes: encabezado, payload y firma
      String[] parts = jwt.split("\\.");
      if (parts.length != 3) {
        throw new IllegalArgumentException("Invalid JWT format");
      }

      // El payload (claims) es la segunda parte, en Base64Url
      String encodedPayload = parts[1];

      // Decodificar el payload de Base64Url a String
      String decodedPayload = new String(
        Base64.getUrlDecoder().decode(encodedPayload)
      );

      // Usar ObjectMapper para convertir el payload decodificado en un objeto Map

      return objectMapper.readValue(decodedPayload, Map.class);
    } catch (JsonProcessingException | IllegalArgumentException e) {
      throw new RuntimeException("Error al decodificar el JWT", e);
    }
  }

  // Verifica el JWT recibido y devuelve el payload o lanza un error
  public Map<String, Object> verifyJwt(String jwt) throws Exception {
    // Paso 1: Decodificar y obtener el payload del JWT
    Map<String, Object> payload = decodeJwt(jwt);

    // Paso 2: Verificar el campo 'aud' (audiencia) del JWT
    String payloadAud = (String) payload.get("aud");
    if (payloadAud == null || !payloadAud.equals(clientId)) {
      throw new IllegalArgumentException("Invalid clientId in aud");
    }

    // Paso 3: Obtener la clave pública del servidor DID
    String didUrl = String.format("%s/oidc/jwks", aud);

    PublicKey publicKey = getPublicKeyFromServer(didUrl);

    // Paso 4: Verificar la firma del JWT usando la clave pública
    verifyJwtSignature(jwt, publicKey);

    // Paso 5: Retornar el payload del JWT
    return payload;
  }

  private void verifyJwtSignature(String jwt, PublicKey publicKey) {
    try {
      Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jwt);
    } catch (JwtException e) {
      throw new IllegalArgumentException(
        "Invalid JWT signature: " + e.getMessage()
      );
    }
  }

  // Obtiene la clave pública desde el servidor DID
  private PublicKey getPublicKeyFromServer(String jwksUrl) throws Exception {
    RestTemplate restTemplate = new RestTemplate();
    String jsonResponse = restTemplate.getForObject(jwksUrl, String.class);

    KeysContainer keyContainer = objectMapper.readValue(
      jsonResponse,
      KeysContainer.class
    );

    // Validar que existan claves en la respuesta
    if (keyContainer.getKeys() == null || keyContainer.getKeys().isEmpty()) {
      throw new IllegalArgumentException("No keys found in the response");
    }

    // Buscar la clave con el `kid` correspondiente
    KeysContainer.Key firstKey = keyContainer
      .getKeys()
      .stream()
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("No keys found"));

    // Validar que la clave tenga la curva esperada
    if (!"P-256".equals(firstKey.getCrv())) {
      throw new IllegalArgumentException(
        "Unsupported curve: " + firstKey.getCrv()
      );
    }

    // Extraer las coordenadas x e y
    String x = firstKey.getX();
    String y = firstKey.getY();

    // Decodificar las coordenadas x e y desde Base64 URL
    byte[] xBytes = Base64.getUrlDecoder().decode(x);
    byte[] yBytes = Base64.getUrlDecoder().decode(y);

    // Crear el punto EC con las coordenadas x e y
    ECPoint ecPoint = new ECPoint(
      new java.math.BigInteger(1, xBytes),
      new java.math.BigInteger(1, yBytes)
    );

    // Obtener los parámetros EC (curva utilizada para la clave pública, secp256r1)
    ECParameterSpec ecParams = getECParameterSpec();

    // Crear la clave pública EC
    ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecPoint, ecParams);
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(
      publicKeySpec
    );

    return publicKey;
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

  public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Date getExpiration(String token) {
    return getClaim(token, Claims::getExpiration);
  }

  private boolean isTokenExpired(String token) {
    return getExpiration(token).before(new Date());
  }
}
