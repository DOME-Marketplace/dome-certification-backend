package com.dekraspain.backend.template.modules.productOffering.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compliances_criteria")
public class CompliancesCriteriaEntity {

  @Id
  @GeneratedValue
  private Long id;

  private String labelLevel;

  private String rulesVersion;

  private String category;

  private String code;

  @Column(length = 1000)
  private String criteria;

  @Column(length = 1000)
  private String link;
}
