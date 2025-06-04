package com.dekraspain.backend.template.spring.security;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsFilterConfig {

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(
      Arrays.asList(
        "https://dome-certification.dome-marketplace-sbx.org",
        "https://dome-certification.dome-marketplace-dev2.org",
        "https://dome-certification.dome-marketplace-prd.org",
        "https://dome-certification.dome-marketplace.org",
        "https://dome-certification.dome-marketplace.eu",
        "http://localhost:4200"
      )
    );
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
