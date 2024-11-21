package com.dekraspain.backend.template.modules.auth.domain.service;

import com.dekraspain.backend.template.modules.auth.application.request.LoginRequest;
import com.dekraspain.backend.template.modules.auth.application.request.RegisterRequest;
import com.dekraspain.backend.template.modules.auth.application.request.VerifiableCredentialPayload;
import com.dekraspain.backend.template.modules.auth.application.response.AuthResponse;
import com.dekraspain.backend.template.modules.user.domain.model.UserRole;
import com.dekraspain.backend.template.modules.user.domain.service.UserService;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.spring.Jwt.IdAuthenticationToken;
import com.dekraspain.backend.template.spring.Jwt.JwtService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService; // Inyección del UserService
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

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

    return AuthResponse
      .builder()
      .acces_token(token)
      .user(userService.mapToDTO(user))
      .build();
  }

  public AuthResponse loginProvider(String didkey) {
    // Autenticación del usuario

    UserEntity user = userService.findByDidkey(didkey);

    // Generación del token JWT
    String token = jwtService.getToken(user);

    //Actualziar lastseen
    userService.updateLastSeen(user);

    return AuthResponse
      .builder()
      .acces_token(token)
      .user(userService.mapToDTO(user))
      .build();
  }

  public AuthResponse loginProviderAndUpdate(String email, String didkey) {
    // Obtención del usuario mediante el UserService
    UserEntity user = userService.findByEmail(email);

    UserEntity userUpdated = userService.updateDidKey(user.getId(), didkey);

    // Autenticación del usuario
    IdAuthenticationToken authenticationToken = new IdAuthenticationToken(
      didkey
    );
    authenticationManager.authenticate(authenticationToken);

    // Generación del token JWT
    String token = jwtService.getToken(userUpdated);

    //Actualziar lastseen
    userService.updateLastSeen(userUpdated);

    return AuthResponse
      .builder()
      .acces_token(token)
      .user(userService.mapToDTO(userUpdated))
      .build();
  }

  public AuthResponse registerProvider(
    VerifiableCredentialPayload.CredentialSubject credentialSubject
  ) {
    // Validar que los datos requeridos no sean nulos
    if (credentialSubject == null || credentialSubject.getMandate() == null) {
      throw new IllegalArgumentException("Mandate data is required");
    }

    List<VerifiableCredentialPayload.Power> power = credentialSubject
      .getMandate()
      .getPower();

    // Determinar el rol del usuario
    UserRole role = (
        power != null &&
        power
          .stream()
          .anyMatch(p -> "certification".equalsIgnoreCase(p.getTmfFunction()))
      )
      ? UserRole.EMPLOYEE
      : UserRole.CUSTOMER;

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

    return AuthResponse
      .builder()
      .acces_token(token)
      .user(userService.mapToDTO(newUser))
      .build();
  }

  public Boolean existsByUsername(String username) {
    return userService.existsByUsername(username);
  }

  public Boolean existsByEmail(String email) {
    return userService.existsByEmail(email);
  }

  public UserEntity updateDidKey(UUID userId, String didKey) {
    return userService.updateDidKey(userId, didKey);
  }

  public Boolean existsByDidkey(String didkey) {
    return userService.existsByDidkey(didkey);
  }
}
