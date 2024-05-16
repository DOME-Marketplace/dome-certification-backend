package com.dekraspain.backend.template.spring.security;

import com.dekraspain.backend.template.modules.auth.domain.provider.AuthenticationProviderImpl;
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

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final AuthenticationProviderImpl authProvider;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    return http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(authRequest ->
        authRequest
          .requestMatchers("/static/**")
          .permitAll()
          .requestMatchers("/api/v1/product-offering/**")
          .authenticated()
          .requestMatchers("/api/v1/send-mail")
          .authenticated()
          .requestMatchers(HttpMethod.GET, "/api/v1/private-route-2")
          .authenticated()
          .requestMatchers(HttpMethod.GET, "/api/v1/user")
          .authenticated()
          .requestMatchers(HttpMethod.OPTIONS)
          .permitAll()
          .requestMatchers("/api-docs/***")
          .permitAll()
          .requestMatchers("/api-docs.html/**")
          .permitAll()
          .requestMatchers("/swagger-ui/**")
          .permitAll()
          .requestMatchers("/auth/**")
          .permitAll()
      )
      .sessionManagement(sessionManager ->
        sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authenticationProvider(authProvider)
      .addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
      )
      .build();
  }
}
