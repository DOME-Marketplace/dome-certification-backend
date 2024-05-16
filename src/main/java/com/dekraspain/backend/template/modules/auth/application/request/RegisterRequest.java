package com.dekraspain.backend.template.modules.auth.application.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

  @Valid
  @NotNull(message = "Username cannot be null")
  @NotBlank(message = "Username cannot be blank")
  @Size(
    min = 3,
    max = 20,
    message = "Username must be between 3 and 20 characters long"
  )
  public String username;

  @NotNull(message = "Password cannot be null")
  @NotBlank(message = "Password cannot be blank")
  @Size(
    min = 6,
    max = 40,
    message = "Password must be between 6 and 40 characters long"
  )
  String password;

  @NotBlank(message = "Email cannot be blank")
  @Size(max = 50, message = "Email must be less than 50 characters long")
  @Email(message = "Email must be a valid email address")
  public String email;

  @NotNull(message = "Firstname cannot be null")
  @NotBlank(message = "Firstname cannot be blank")
  @Size(max = 50, message = "Firstname must be less than 50 characters long")
  public String firstname;

  @NotBlank(message = "Lastname cannot be blank")
  @Size(max = 50, message = "Lastname must be less than 50 characters long")
  public String lastname;

  public String country_code;
  public String address;

  public String organization_name;

  public String website;
}
