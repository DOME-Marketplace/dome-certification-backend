package com.dekraspain.backend.template.modules.productOffering.application.controller;

import com.dekraspain.backend.template.modules.productOffering.persistence.entity.CompliancesCriteriaEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.CompliancesCriteriaRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Compliance Criteria")
@RestController
@RequestMapping("/api/v1/compliances-criteria")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class CompliancesCriteriaController {

  private final CompliancesCriteriaRepository repository;

  @GetMapping
  public List<CompliancesCriteriaEntity> getAllByLabelLevel(
    @RequestParam(required = false) String labelLevel
  ) {
    if (labelLevel == null || labelLevel.isEmpty()) {
      return repository.findAll();
    }
    return repository.findByLabelLevel(labelLevel.toUpperCase());
  }
}
