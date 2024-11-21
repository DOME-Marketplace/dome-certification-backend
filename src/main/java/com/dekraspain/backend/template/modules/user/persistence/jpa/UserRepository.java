package com.dekraspain.backend.template.modules.user.persistence.jpa;

import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByUsernameOrEmail(String username, String email);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

  Optional<UserEntity> findByEmail(String email);

  Boolean existsByDidkey(String didkey);

  Optional<UserEntity> findByDidkey(String didkey);
}
