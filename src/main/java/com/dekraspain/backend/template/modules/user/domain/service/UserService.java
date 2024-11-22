package com.dekraspain.backend.template.modules.user.domain.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.domain.model.UserRole;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.modules.user.persistence.jpa.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public UserEntity createUser(
    String username,
    String email,
    String password,
    String firstname,
    String lastname,
    String countryCode,
    String address,
    String organizationName,
    String website,
    UserRole role
  ) {
    UserEntity userEntity = UserEntity
      .builder()
      .username(username)
      .email(email)
      .password(password)
      .firstname(firstname)
      .lastname(lastname)
      .country_code(countryCode)
      .address(address)
      .organization_name(organizationName)
      .website(website)
      .role(role)
      .build();

    return userRepository.save(userEntity);
  }

  public UserEntity createUserProvider(
    String email,
    String firstname,
    String lastname,
    String countryCode,
    String organizationName,
    String didkey,
    UserRole role
  ) {
    UserEntity userEntity = UserEntity
      .builder()
      .email(email)
      .firstname(firstname)
      .lastname(lastname)
      .country_code(countryCode)
      .organization_name(organizationName)
      .role(role)
      .didkey(didkey)
      .last_seen(new Date())
      .build();

    return userRepository.save(userEntity);
  }

  public UserEntity getUserById(String id) {
    return userRepository
      .findById(UUID.fromString(id))
      .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  @Transactional
  public void updateLastSeen(UserEntity user) {
    user.setLast_seen(new Date());
    userRepository.save(user);
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public Boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  public boolean existsByDidkey(String didkey) {
    return userRepository.existsByDidkey(didkey);
  }

  public UserEntity findByEmail(String email) {
    return userRepository
      .findByEmail(email)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public UserEntity findByDidkey(String didkey) {
    return userRepository
      .findByDidkey(didkey)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));
  }

  public UserDTO mapToDTO(UserEntity user) {
    return UserDTO
      .builder()
      .id(user.getId().toString())
      .username(user.getUsername())
      .firstname(user.getFirstname())
      .lastname(user.getLastname())
      .country_code(user.getCountry_code())
      .address(user.getAddress())
      .organization_name(user.getOrganization_name())
      .website(user.getWebsite())
      .last_seen(user.getLast_seen())
      .role(user.getRole())
      .build();
  }

  public UserEntity getUserByUsername(String username) {
    return userRepository
      .findByUsername(username)
      .orElseThrow(() ->
        new IllegalArgumentException(
          "User not found with username: " + username
        )
      );
  }

  public UserEntity getUserByUsernameOrEmail(String usernameOrEmail) {
    return userRepository
      .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
      .orElseThrow(() ->
        new IllegalArgumentException(
          "User not found with username or email: " + usernameOrEmail
        )
      );
  }

  public UserEntity updateDidKeyAndRole(
    UserEntity user,
    String didKey,
    UserRole role
  ) {
    user.setDidkey(didKey);
    user.setRole(role);
    return userRepository.save(user);
  }

  public UserEntity updateRole(UserEntity user, UserRole role) {
    user.setRole(role);
    return userRepository.save(user);
  }
}
