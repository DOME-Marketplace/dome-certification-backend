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
  String firstname;
  String lastname;
  String country_code;
  String address;
  String organization_name;
  String website;
  Date last_seen;
  UserRole role;
}
