package com.dekraspain.backend.template.modules.user.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dekraspain.backend.template.modules.user.persistence.entity.AccesLogEntity;

public interface AccessLogRepository
  extends JpaRepository<AccesLogEntity, Integer> {}
