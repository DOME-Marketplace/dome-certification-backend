package com.dekraspain.backend.template.modules.user.application.controller;

import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile")
@RestController
@RequestMapping(value = "/api/v1/user")
@RequiredArgsConstructor
@CrossOrigin(
  { "http://localhost:4200", "https://xs86qb08-4200.uks1.devtunnels.ms" }
)
public class UserController {

  private final UserService userService;

  @Operation(security = { @SecurityRequirement(name = "bearer-key") })
  @GetMapping(value = "{UID}")
  public ResponseEntity<UserDTO> getUser(@PathVariable String UID) {
    UserDTO userDTO = userService.getUser(UID);
    if (userDTO == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userDTO);
  }
}
