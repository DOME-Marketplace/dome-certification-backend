package com.dekraspain.backend.template.modules.user.application.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

  public String id;
  public String username;
  public String firstname;
  public String lastname;
  public String country;
}
