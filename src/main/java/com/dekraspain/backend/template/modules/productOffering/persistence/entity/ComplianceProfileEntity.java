package com.dekraspain.backend.template.modules.productOffering.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compliance_profile")
public class ComplianceProfileEntity {

  @Id
  @GeneratedValue
  private Long id;

  private String fileName;

  private String url;
  private String hash;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_offering_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ProductOfferingEntity productOffering;
}
