package com.dekraspain.backend.template.modules.user.domain.service;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.dekraspain.backend.template.modules.user.persistence.entity.AccesLogEntity;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import com.dekraspain.backend.template.modules.user.persistence.jpa.AccessLogRepository;

@Service
public class AccessLogService {

  private final AccessLogRepository accessLogRepository;

  public AccessLogService(AccessLogRepository accessLogRepository) {
    this.accessLogRepository = accessLogRepository;
  }

  public void logAccess(UserEntity user) {
    AccesLogEntity logEntry = AccesLogEntity
      .builder()
      .accessDate(new Date())
      .user(user)
      .build();

    accessLogRepository.save(logEntry);
  }
}
