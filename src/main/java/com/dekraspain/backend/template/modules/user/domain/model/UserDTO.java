package com.dekraspain.backend.template.modules.user.domain.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

  String id;
  String username;
  String email;
  String firstname;
  String lastname;
  String organization_country_code;
  String organization_name;
  String organization_id;
  String organization_email;
  Date last_seen;
  UserRole role;
}
