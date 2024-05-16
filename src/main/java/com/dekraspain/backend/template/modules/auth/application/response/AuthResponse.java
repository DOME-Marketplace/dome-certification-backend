package com.dekraspain.backend.template.modules.auth.application.response;

import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

  String acces_token;
  UserDTO user;
}
