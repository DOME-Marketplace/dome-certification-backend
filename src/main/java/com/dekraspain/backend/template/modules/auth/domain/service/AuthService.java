package com.dekraspain.backend.template.modules.auth.domain.service;

import com.dekraspain.backend.template.modules.auth.application.request.LoginRequest;
import com.dekraspain.backend.template.modules.auth.application.request.RegisterRequest;
import com.dekraspain.backend.template.modules.auth.application.response.AuthResponse;
import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.domain.model.UserRole;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.modules.user.persistence.jpa.UserRepository;
import com.dekraspain.backend.template.spring.Jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        request.getUsername(),
        request.getPassword()
      )
    );
    UserEntity user = userRepository
      .findByUsername(request.getUsername())
      .orElseThrow();

    String token = jwtService.getToken(user);

    UserDTO userDTO = UserDTO
      .builder()
      .id(String.valueOf(user.getId()))
      .username(user.getUsername())
      .role(user.getRole())
      .firstname(user.getFirstname())
      .lastname(user.getLastname())
      .country_code(user.getCountry_code())
      .address(user.getAddress())
      .organization_name(user.getOrganization_name())
      .website(user.getWebsite())
      .build();

    return AuthResponse.builder().acces_token(token).user(userDTO).build();
  }

  public Boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  public Boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public AuthResponse register(RegisterRequest request) {
    UserEntity user = UserEntity
      .builder()
      .username(request.getUsername())
      .email(request.getEmail())
      .password(passwordEncoder.encode(request.getPassword()))
      .firstname(request.getFirstname())
      .lastname(request.lastname)
      .country_code(request.getCountry_code())
      .address(request.getAddress())
      .organization_name(request.getOrganization_name())
      .website(request.getWebsite())
      .role(UserRole.CUSTOMER)
      .build();

    userRepository.save(user);

    UserDTO userDTO = UserDTO
      .builder()
      .id(String.valueOf(user.getId()))
      .username(user.getUsername())
      .role(user.getRole())
      .firstname(user.getFirstname())
      .lastname(user.getLastname())
      .country_code(user.getCountry_code())
      .address(user.getAddress())
      .organization_name(user.getOrganization_name())
      .website(user.getWebsite())
      .build();

    return AuthResponse
      .builder()
      .acces_token(jwtService.getToken(user))
      .user(userDTO)
      .build();
  }
}
