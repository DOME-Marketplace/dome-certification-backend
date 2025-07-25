package com.dekraspain.backend.template.modules.productOffering.persistence.entity;

import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStates;
import com.dekraspain.backend.template.modules.productOffering.domain.model.RequestedComplianceLevel;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_offering")
public class ProductOfferingEntity {

  @Id
  @GeneratedValue
  private Long id;

  private String id_PO;
  private String service_name;
  private String service_version;
  private String name_organization;
  private String address_organization;
  private String ISO_Country_Code;
  private String url_organization;
  private String email_organization;
  private String VAT_ID;
  private String comments;

  // Nuevo campo: Requested Compliance Level como Value Object
  @jakarta.persistence.Embedded
  private RequestedComplianceLevel requestedComplianceLevel;

  @ManyToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "issuer")
  private UserEntity issuer;

  @ManyToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserEntity user;

  private String image;

  @Enumerated(EnumType.STRING)
  private ProductOfferingStates status;

  public Date request_date;
  private Date issue_date;
  private Date expiration_date;

  @Column(nullable = false, columnDefinition = "boolean default false")
  private Boolean isExpirationWarningEmailSent;

  @Column(nullable = false, columnDefinition = "boolean default false")
  private Boolean isExpirationEmailSent;

  @OneToMany(
    mappedBy = "productOffering",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private final List<ComplianceProfileEntity> complianceProfiles = new ArrayList<>();

  @OneToMany(
    mappedBy = "productOffering",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  private final List<ComplianceEntity> compliances = new ArrayList<>();
}
