package com.dekraspain.backend.template.modules.productOffering.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_offering_compliance_profile_criteria")
public class ProductOfferingComplianceProfileCriteriaEntity {

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = new java.util.Date();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_offering_id", nullable = false)
    private ProductOfferingEntity productOffering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compliances_criteria_id", nullable = false)
    private CompliancesCriteriaEntity compliancesCriteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compliance_profile_id", nullable = false)
    private ComplianceProfileEntity complianceProfile;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id", nullable = false)
    private com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity issuer;
}
