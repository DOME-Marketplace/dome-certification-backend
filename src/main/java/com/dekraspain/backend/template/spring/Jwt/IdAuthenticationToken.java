package com.dekraspain.backend.template.spring.Jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class IdAuthenticationToken extends AbstractAuthenticationToken {

  private final String didkey; // Identificador único (DIDKey)
  private Object principal; // El usuario autenticado

  // Constructor para cuando no se ha autenticado todavía (solo el didkey)
  public IdAuthenticationToken(String didkey) {
    super(null); // No hay authorities al principio
    this.didkey = didkey;
    this.setAuthenticated(false); // Aún no autenticado
  }

  // Constructor cuando ya se ha autenticado el usuario
  public IdAuthenticationToken(String didkey, Object principal) {
    super(null); // No hay authorities al principio
    this.didkey = didkey;
    this.principal = principal;
    this.setAuthenticated(true); // Ahora está autenticado
  }

  public String getDidkey() {
    return didkey;
  }

  @Override
  public Object getCredentials() {
    return null; // No necesitamos contraseñas
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  public void setPrincipal(Object principal) {
    this.principal = principal;
  }
}
