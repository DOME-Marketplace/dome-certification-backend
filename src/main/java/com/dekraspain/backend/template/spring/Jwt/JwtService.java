package com.dekraspain.backend.template.spring.Jwt;

import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret.key}")
  private String secretKey;

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

  private Key getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
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
