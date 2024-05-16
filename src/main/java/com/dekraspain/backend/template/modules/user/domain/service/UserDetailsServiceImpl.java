package com.dekraspain.backend.template.modules.user.domain.service;

import com.dekraspain.backend.template.modules.user.persistence.jpa.UserRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String id) {
    return userRepository.findById(UUID.fromString(id)).orElse(null);
  }
}
