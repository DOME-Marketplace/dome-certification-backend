package com.dekraspain.backend.template.shared.email.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailDTO {

  @NotBlank
  @Email
  public String to;

  @NotBlank
  public String subject;

  @NotBlank
  public String title;

  @NotBlank
  public String content;
}
