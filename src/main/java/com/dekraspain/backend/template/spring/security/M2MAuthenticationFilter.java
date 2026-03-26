package com.dekraspain.backend.template.spring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lightweight filter to validate a machine-to-machine token for specific endpoint(s).
 * It expects the Authorization header with a Bearer token (Authorization: Bearer <token>).
 * On successful verification the filter populates the SecurityContext with a simple
 * Authentication so the request can proceed as an authenticated machine client.
 */
public class M2MAuthenticationFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(M2MAuthenticationFilter.class);

  public static final String AUTH_HEADER = "Authorization";
  private final M2MTokenVerifier tokenVerifier;
    public M2MAuthenticationFilter(M2MTokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
    }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    // Only filter POST /api/v1/product-offering/certificate
    String path = request.getRequestURI();
    return !(
      HttpMethod.POST.matches(request.getMethod()) &&
      path != null && path.endsWith("/api/v1/product-offering/certificate")
    );
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String authHeader = request.getHeader(AUTH_HEADER);
    String token = null;

    if (StringUtils.hasText(authHeader) && authHeader.toLowerCase().startsWith("bearer ")) {
      token = authHeader.substring(7).trim();
    }

    if (!StringUtils.hasText(token)) {
      log.debug("M2M filter: Authorization header missing or not Bearer for request {} {}", request.getMethod(), request.getRequestURI());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("M2M token missing or invalid");
      return;
    }

    M2MTokenVerificationResult result = M2MTokenVerificationResult.other("unknown");
    try {
      result = tokenVerifier.verify(token);
    } catch (Exception ex) {
      log.error("M2M filter: error while authenticating token", ex);
      result = M2MTokenVerificationResult.other(ex.getMessage());
    }

    if (!result.isValid()) {
      log.info("M2M filter: token verification failed ({}) for request {} {}", result.getReason(), request.getMethod(), request.getRequestURI());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      String msg = switch (result.getReason()) {
        case EXPIRED -> "M2M token expired: " + result.getMessage();
        case INVALID_SIGNATURE -> "M2M token signature invalid: " + result.getMessage();
        case INVALID_FORMAT -> "M2M token format invalid: " + result.getMessage();
        case UNSUPPORTED_ALGORITHM -> "M2M token uses unsupported algorithm: " + result.getMessage();
        case KID_NOT_FOUND -> "M2M token kid not found in JWKS: " + result.getMessage();
        case JWKS_FETCH_ERROR -> "Error fetching JWKS: " + result.getMessage();
        default -> "M2M token invalid: " + result.getMessage();
      };
      response.getWriter().write(msg);
      return;
    }

    log.info("M2M filter: token validated, setting M2M authentication for request {} {}", request.getMethod(), request.getRequestURI());

    // create a simple authentication for downstream code. Grant a role 'M2M_CLIENT'.
    Authentication auth = new UsernamePasswordAuthenticationToken(
      "m2m-client",
      null,
      java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_M2M_CLIENT"))
    );

    SecurityContextHolder.getContext().setAuthentication(auth);
    filterChain.doFilter(request, response);
  }
}
