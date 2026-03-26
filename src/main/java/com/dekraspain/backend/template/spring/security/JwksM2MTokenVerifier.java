package com.dekraspain.backend.template.spring.security;

import com.dekraspain.backend.template.modules.auth.application.request.KeysContainer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.net.URI;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class JwksM2MTokenVerifier implements M2MTokenVerifier {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private volatile KeysContainer cachedKeys;
  private volatile long fetchedAt = 0L;
  private final long ttl = 5 * 60 * 1000;

  @Value("${jwt.oauth.aud}")
  private String verifiyerUrl;

  private KeysContainer fetchJwks(String jwksUrl) throws Exception {
    long now = System.currentTimeMillis();
    if (cachedKeys == null || (now - fetchedAt) > ttl) {
      RestTemplate rt = new RestTemplate();
      String json = rt.getForObject(new URI(jwksUrl), String.class);
      cachedKeys = objectMapper.readValue(json, new TypeReference<KeysContainer>() {});
      fetchedAt = now;
    }
    return cachedKeys;
  }

  private PublicKey toPublicKey(KeysContainer.Key key) throws Exception {
    // Only supports P-256 (secp256r1)
    byte[] x = Base64.getUrlDecoder().decode(key.getX());
    byte[] y = Base64.getUrlDecoder().decode(key.getY());
  ECPoint point = new ECPoint(new java.math.BigInteger(1, x), new java.math.BigInteger(1, y));
  // Build parameters by generating a key pair and taking its params (same approach used elsewhere)
  java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("EC");
  kpg.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"));
  ECParameterSpec params = ((ECPublicKey) kpg.genKeyPair().getPublic()).getParams();

  ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, params);
    KeyFactory kf = KeyFactory.getInstance("EC");
    return (ECPublicKey) kf.generatePublic(pubSpec);
  }

  @Override
  public M2MTokenVerificationResult verify(String jwt) {
    try {
      String[] parts = jwt.split("\\.");
      if (parts.length != 3) return M2MTokenVerificationResult.invalidFormat("Invalid JWT format");
      String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
      Map<String, Object> header = objectMapper.readValue(headerJson, new TypeReference<Map<String, Object>>() {});
      String kid = header.getOrDefault("kid", "").toString();
      String alg = header.getOrDefault("alg", "").toString();
      if (!alg.toUpperCase().startsWith("ES")) return M2MTokenVerificationResult.unsupportedAlg("Unsupported alg: " + alg);

      String jwksUrl = verifiyerUrl + "/oidc/jwks";
      KeysContainer keys;
      try {
        keys = fetchJwks(jwksUrl);
      } catch (Exception ex) {
        return M2MTokenVerificationResult.jwksFetchError("Error fetching JWKS: " + ex.getMessage());
      }

      KeysContainer.Key match = null;
      if (kid != null && !kid.isEmpty()) {
        match = keys.getKeys().stream().filter(k -> kid.equals(k.getKid())).findFirst().orElse(null);
      }

      if (match == null) {
        return M2MTokenVerificationResult.kidNotFound("kid not found in JWKS: " + kid);
      }

      PublicKey pk = toPublicKey(match);
      try {
        Jwts.parserBuilder().setSigningKey(pk).build().parseClaimsJws(jwt);
      } catch (io.jsonwebtoken.ExpiredJwtException eje) {
        return M2MTokenVerificationResult.expired(eje.getMessage());
      } catch (JwtException je) {
        return M2MTokenVerificationResult.invalidSignature(je.getMessage());
      }

      return M2MTokenVerificationResult.ok();
    } catch (com.fasterxml.jackson.core.JsonProcessingException jpe) {
      return M2MTokenVerificationResult.invalidFormat("Header JSON invalid: " + jpe.getMessage());
    } catch (Exception e) {
      return M2MTokenVerificationResult.other(e.getMessage());
    }
  }
}
