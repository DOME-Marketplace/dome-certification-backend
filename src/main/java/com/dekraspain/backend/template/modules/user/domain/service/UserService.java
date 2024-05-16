package com.dekraspain.backend.template.modules.user.domain.service;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.dekraspain.backend.template.modules.user.domain.model.UserDTO;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final EntityManager entityManager;

  public UserDTO getUser(String id) {
    UserEntity userEntity = entityManager
      .createQuery(
        "SELECT u FROM UserEntity u WHERE u.id = :id",
        UserEntity.class
      )
      .setParameter("id", id)
      .getSingleResult();

    return UserDTO
      .builder()
      .id(userEntity.getId().toString())
      .username(userEntity.getUsername())
      .firstname(userEntity.getFirstname())
      .lastname(userEntity.getLastname())
      .country_code(userEntity.getCountry_code())
      .address(userEntity.getAddress())
      .organization_name(userEntity.getOrganization_name())
      .website(userEntity.getWebsite())
      .last_seen(userEntity.getLast_seen())
      .build();
  }

  @Transactional
  public void updateLastSeen(String username) {
    UserEntity user = entityManager
      .createQuery(
        "SELECT u FROM UserEntity u WHERE u.username = :username",
        UserEntity.class
      )
      .setParameter("username", username)
      .getSingleResult();
    user.setLast_seen(new Date());
    entityManager.merge(user);
  }
}
