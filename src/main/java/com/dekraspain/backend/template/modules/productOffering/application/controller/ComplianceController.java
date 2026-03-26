package com.dekraspain.backend.template.modules.productOffering.application.controller;

import com.dekraspain.backend.template.modules.productOffering.domain.model.ComplianceStandardsDTO;
import com.dekraspain.backend.template.modules.productOffering.domain.service.ComplianceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Compliance Standards")
@RestController
@RequestMapping("/api/v1/compliance-standards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class ComplianceController {

  private final ComplianceService complianceService;

  @GetMapping(value = "/")
  public ResponseEntity<List<ComplianceStandardsDTO>> getAll() {
    List<ComplianceStandardsDTO> compliancesStandars = complianceService.getAllCompliances();
    return ResponseEntity.ok(compliancesStandars);
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<ComplianceStandardsDTO> getById(@PathVariable Long id) {
    ComplianceStandardsDTO compliance = complianceService.getComplianceStandardById(
      id
    );
    return ResponseEntity.ok(compliance);
  }
}
