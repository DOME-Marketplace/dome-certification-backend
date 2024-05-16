package com.dekraspain.backend.template.modules.auth.application.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

  @Valid
  @NotNull(message = "Username cannot be null")
  @NotBlank(message = "Username cannot be blank")
  @Size(
    min = 3,
    max = 20,
    message = "Username must be between 3 and 20 characters long"
  )
  public String username;

  @NotBlank(message = "Password cannot be blank")
  @NotNull(message = "Password cannot be null")
  @Size(
    min = 6,
    max = 40,
    message = "Password must be between 6 and 40 characters long"
  )
  String password;
}
