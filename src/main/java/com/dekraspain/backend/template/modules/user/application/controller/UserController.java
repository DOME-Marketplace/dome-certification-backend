package com.dekraspain.backend.template.modules.user.application.controller;

import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.domain.service.UserService;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile")
@RestController
@RequestMapping(value = "/api/v1/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(security = { @SecurityRequirement(name = "bearer-key") })
  @GetMapping(value = "me")
  public ResponseEntity<UserDTO> me(@AuthenticationPrincipal UserEntity user) {
    // Mapea el UserEntity a UserDTO
    UserDTO userDTO = userService.mapToDTO(user);

    if (userDTO == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(userDTO);
  }
}
