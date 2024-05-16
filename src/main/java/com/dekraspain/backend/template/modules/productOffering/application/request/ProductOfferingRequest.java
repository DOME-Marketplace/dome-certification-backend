package com.dekraspain.backend.template.modules.productOffering.application.request;

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
public class ProductOfferingRequest {

  @NotNull(message = "Service name cannot be null")
  @NotBlank(message = "Service name cannot be blank")
  @Size(
    min = 3,
    max = 40,
    message = "Service name must be between 3 and 40 characters long"
  )
  public String service_name;

  @NotNull(message = "Service version cannot be null")
  @NotBlank(message = "Service version cannot be blank")
  @Size(
    min = 3,
    max = 3,
    message = "Service version must be in this format: 0.0"
  )
  public String service_version;

  @NotNull(message = "Name organization cannot be null")
  @NotBlank(message = "Name organization cannot be blank")
  @Size(
    min = 3,
    max = 40,
    message = "Name organization must be between 3 and 40 characters long"
  )
  public String name_organization;

  @NotNull(message = "Address organization cannot be null")
  @NotBlank(message = "Address organization cannot be blank")
  @Size(
    min = 3,
    max = 60,
    message = "Address organization must be between 3 and 40 characters long"
  )
  public String address_organization;

  @NotNull(message = "ISO Country Code cannot be null")
  @NotBlank(message = "ISO Country Code cannot be blank")
  @Size(
    min = 2,
    max = 2,
    message = "ISO Country Code must be in this format: XX"
  )
  public String ISO_Country_Code;

  @NotNull(message = "URL organization cannot be null")
  @NotBlank(message = "URL organization cannot be blank")
  @Size(
    min = 3,
    max = 60,
    message = "URL organization must be between 3 and 60 characters long"
  )
  public String url_organization;

  @NotNull(message = "Email organization cannot be null")
  @NotBlank(message = "Email organization cannot be blank")
  @Size(
    min = 3,
    max = 60,
    message = "Email organization must be between 3 and 60 characters long"
  )
  @Email
  public String email_organization;

  @NotNull(message = "ID PO cannot be null")
  @NotBlank(message = "ID PO cannot be blank")
  @Size(
    min = 3,
    max = 40,
    message = "ID PO must be between 3 and 40 characters long"
  )
  public String id_PO;
}
