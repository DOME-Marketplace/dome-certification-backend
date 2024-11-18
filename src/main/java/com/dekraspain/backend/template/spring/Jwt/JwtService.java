package com.dekraspain.backend.template.spring.Jwt;

import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

  @Value("${jwt.oauth.response-type}")
  private String responseType;

  @Value("${jwt.oauth.scope}")
  private String scope;

  @Value("${jwt.oauth.aud}")
  private String aud;

  private final long expirationTime = 86400000;

  public String getToken(UserEntity user) {
    return getToken(new HashMap<>(), user);
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
  public String generateOauthToken() {
    try {
      Claims claims = Jwts.claims();
      claims.put("iss", clientId);
      claims.put("sub", clientId);
      claims.put("aud", aud);
      claims.put("jti", UUID.randomUUID().toString());

      return Jwts
        .builder()
        .setClaims(claims)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(loadPrivateKey(), SignatureAlgorithm.ES256)
        .compact();
    } catch (Exception e) {
      throw new RuntimeException("Error generating OAuth token", e);
    }
  }

  private Key getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public ECPrivateKey loadPrivateKey() throws Exception {
    // Decodificar las coordenadas de Base64
    byte[] dBytes = Base64.getUrlDecoder().decode(d);
    // byte[] xBytes = Base64.getUrlDecoder().decode(x);
    // byte[] yBytes = Base64.getUrlDecoder().decode(y);

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

  // Método para obtener el ECParameterSpec (específica de P-256)
  private ECParameterSpec getECParameterSpec()
    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    // Usar ECGenParameterSpec para especificar la curva P-256
    ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1"); // "secp256r1" es equivalente a P-256
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
    keyPairGenerator.initialize(ecSpec);
    ECParameterSpec ecParams =
      ((ECPublicKey) keyPairGenerator.genKeyPair().getPublic()).getParams();
    return ecParams;
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
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(decodedPayload, Map.class);
    } catch (JsonProcessingException | IllegalArgumentException e) {
      throw new RuntimeException("Error al decodificar el JWT", e);
    }
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
