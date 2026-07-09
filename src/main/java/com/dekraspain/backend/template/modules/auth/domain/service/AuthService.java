package com.dekraspain.backend.template.modules.auth.domain.service;

import com.dekraspain.backend.template.modules.auth.application.request.VerifiableCredentialPayload;
import com.dekraspain.backend.template.modules.auth.application.response.AuthResponse;
import com.dekraspain.backend.template.modules.user.domain.model.UserRole;
import com.dekraspain.backend.template.modules.user.domain.service.AccessLogService;
import com.dekraspain.backend.template.modules.user.domain.service.UserService;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.spring.Jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final JwtService jwtService;
  private final AccessLogService accessLogService;

  public AuthResponse loginProvider(
    String didkey,
    UserRole role,
    String organizationId,
    String organizationEmail
  ) {
    // Autenticación del usuario

    UserEntity user = userService.findByDidkey(didkey);

    UserEntity userUpdated = userService.updateRole(user, role);
    userService.updateOrganizationId(userUpdated, organizationId);
    userService.updateOrganizationEmail(userUpdated, organizationEmail);

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
    UserRole role,
    String organizationId,
    String organizationEmail
  ) {
    // Obtención del usuario mediante el UserService
    UserEntity user = userService.findByEmail(email);

    UserEntity userUpdated = userService.updateDidKeyAndRole(
      user,
      didkey,
      role
    );
    userService.updateOrganizationId(userUpdated, organizationId);
    userService.updateOrganizationEmail(userUpdated, organizationEmail);
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
    // El id del mandatee es opcional; se normaliza a null cuando no viene
    // para no chocar con la restricción de unicidad de didkey.
    String rawDidkey = credentialSubject.getMandate().getMandatee().getId();
    String didkey = (rawDidkey != null && !rawDidkey.isBlank())
      ? rawDidkey
      : null;

    // Creación del nuevo usuario
    UserEntity newUser = userService.createUserProvider(
      credentialSubject.getMandate().getMandatee().getEmail(),
      credentialSubject.getMandate().getMandatee().getFirst_name(),
      credentialSubject.getMandate().getMandatee().getLast_name(),
      credentialSubject.getMandate().getMandator().getCountry(),
      credentialSubject.getMandate().getMandator().getOrganization(),
      didkey,
      role,
      credentialSubject.getMandate().getMandator().getOrganizationIdentifier(),
      credentialSubject.getMandate().getMandator().getEmail()
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
