package com.dekraspain.backend.template.spring.security;

import com.dekraspain.backend.template.modules.auth.domain.provider.AuthenticationProviderImpl;
import com.dekraspain.backend.template.modules.user.domain.model.UserRole;
import com.dekraspain.backend.template.spring.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {

  private static final String BASE_PATH_V1 = "/api/v1";

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final AuthenticationProviderImpl authProvider;
  private final com.dekraspain.backend.template.spring.security.M2MTokenVerifier m2mTokenVerifier;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    return http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(authRequest ->
        authRequest
          .requestMatchers("/static/**")
          .permitAll()
          // Allow the M2M create endpoint to be processed by the M2M filter.
          // The filter itself will return 401 if the M2M token is missing/invalid.
          .requestMatchers(HttpMethod.POST, BASE_PATH_V1 + "/product-offering/certificate")
          .permitAll()
          .requestMatchers(
            HttpMethod.POST,
            BASE_PATH_V1 + "/product-offering/issuances"
          )
          .hasAnyAuthority(UserRole.EMPLOYEE.name(), UserRole.ADMIN.name())
          .requestMatchers(HttpMethod.GET, BASE_PATH_V1 + "/compliances/by-product/**")
          .authenticated()
          .requestMatchers(HttpMethod.GET, BASE_PATH_V1 + "/external-product-offering/**")
          .authenticated()
          .requestMatchers(BASE_PATH_V1 + "/product-offering/**")
          .authenticated()
          .requestMatchers(BASE_PATH_V1 + "/compliance-standards/**")
          .authenticated()
          .requestMatchers(BASE_PATH_V1 + "/compliances-criteria/**")
          .authenticated()
          .requestMatchers(BASE_PATH_V1 + "/send-mail")
          .authenticated()
          .requestMatchers(HttpMethod.GET, BASE_PATH_V1 + "/user/**")
          .authenticated()
          .requestMatchers(HttpMethod.OPTIONS)
          .permitAll()
          .requestMatchers("/api-docs/***")
          .permitAll()
          .requestMatchers("/api-docs.html/**")
          .permitAll()
          .requestMatchers("/swagger-ui/**")
          .permitAll()
          .requestMatchers(HttpMethod.GET, "/auth/client-assertion-token-m2m")
          .hasAnyAuthority(UserRole.EMPLOYEE.name(), UserRole.ADMIN.name())
          .requestMatchers("/auth/**")
          .permitAll()
      // /auth/client-assertion-token-m2m protegido con rol de eployee
      )
      .sessionManagement(sessionManager ->
        sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authenticationProvider(authProvider)
      .addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
      )
      // M2M filter should run before the JWT filter so it can authenticate machine clients
      .addFilterBefore(
        m2mAuthenticationFilter(),
        JwtAuthenticationFilter.class
      )
      .build();
  }

  @Bean
  public M2MAuthenticationFilter m2mAuthenticationFilter() {
    return new M2MAuthenticationFilter(m2mTokenVerifier);
  }
}
