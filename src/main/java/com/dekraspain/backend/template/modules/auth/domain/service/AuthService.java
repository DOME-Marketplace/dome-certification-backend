package com.dekraspain.backend.template.modules.auth.domain.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dekraspain.backend.template.modules.auth.application.request.LoginRequest;
import com.dekraspain.backend.template.modules.auth.application.request.RegisterRequest;
import com.dekraspain.backend.template.modules.auth.application.request.VerifiableCredentialPayload;
import com.dekraspain.backend.template.modules.auth.application.response.AuthResponse;
import com.dekraspain.backend.template.modules.user.domain.model.UserRole;
import com.dekraspain.backend.template.modules.user.domain.service.AccessLogService;
import com.dekraspain.backend.template.modules.user.domain.service.UserService;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.spring.Jwt.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService; // Inyección del UserService
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final AccessLogService accessLogService;

  public AuthResponse login(LoginRequest request) {
    // Obtención del usuario mediante el UserService
    UserEntity user = userService.getUserByUsernameOrEmail(
      request.getUsername()
    );
    // Autenticación del usuario
    authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(user, request.getPassword())
    );

    // Generación del token JWT
    String token = jwtService.getToken(user);

    //Actualziar lastseen
    userService.updateLastSeen(user);
    accessLogService.logAccess(user);

    return AuthResponse
      .builder()
      .acces_token(token)
      .user(userService.mapToDTO(user))
      .build();
  }

  public AuthResponse loginProvider(String didkey, UserRole role) {
    // Autenticación del usuario

    UserEntity user = userService.findByDidkey(didkey);

    UserEntity userUpdated = userService.updateRole(user, role);
    // Generación del token JWT
    String token = jwtService.getToken(userUpdated);

    //Actualziar lastseen
    userService.updateLastSeen(userUpdated);
    accessLogService.logAccess(user);

    return AuthResponse
      .builder()
      .acces_token(token)
      .user(userService.mapToDTO(userUpdated))
      .build();
  }

  public AuthResponse loginProviderAndUpdate(
    String email,
    String didkey,
    UserRole role
  ) {
    // Obtención del usuario mediante el UserService
    UserEntity user = userService.findByEmail(email);

    UserEntity userUpdated = userService.updateDidKeyAndRole(
      user,
      didkey,
      role
    );

    // Generación del token JWT
    String token = jwtService.getToken(userUpdated);

    //Actualziar lastseen
    userService.updateLastSeen(userUpdated);
    accessLogService.logAccess(user);

    return AuthResponse
      .builder()
      .acces_token(token)
      .user(userService.mapToDTO(userUpdated))
      .build();
  }

  public AuthResponse registerProvider(
    VerifiableCredentialPayload.CredentialSubject credentialSubject,
    UserRole role
  ) {
    // Creación del nuevo usuario
    UserEntity newUser = userService.createUserProvider(
      credentialSubject.getMandate().getMandatee().getEmail(),
      credentialSubject.getMandate().getMandatee().getFirstName(),
      credentialSubject.getMandate().getMandatee().getLastName(),
      credentialSubject.getMandate().getMandator().getCountry(),
      credentialSubject.getMandate().getMandator().getOrganization(),
      credentialSubject.getMandate().getMandatee().getId(),
      role
    );

    // Generación del token JWT
    String token = jwtService.getToken(newUser);
    accessLogService.logAccess(newUser);
    return AuthResponse
      .builder()
      .acces_token(token)
      .user(userService.mapToDTO(newUser))
      .build();
  }

  public AuthResponse register(RegisterRequest request) {
    // Creación del nuevo usuario
    UserEntity newUser = userService.createUser(
      request.getUsername(),
      request.getEmail(),
      passwordEncoder.encode(request.getPassword()),
      request.getFirstname(),
      request.getLastname(),
      request.getCountry_code(),
      request.getAddress(),
      request.getOrganization_name(),
      request.getWebsite(),
      UserRole.CUSTOMER // Rol predeterminado
    );

    // Generación del token JWT
    String token = jwtService.getToken(newUser);
    accessLogService.logAccess(newUser);

    return AuthResponse
      .builder()
      .acces_token(token)
      .user(userService.mapToDTO(newUser))
      .build();
  }
}
