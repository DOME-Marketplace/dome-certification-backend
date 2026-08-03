package com.dekraspain.backend.template.modules.productOffering.persistence.init;

import com.dekraspain.backend.template.modules.productOffering.persistence.entity.CompliancesCriteriaEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.CompliancesCriteriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CompliancesCriteriaDataLoader implements CommandLineRunner {

  @Autowired
  private CompliancesCriteriaRepository repository;

  private static final String RULES_VERSION_CD2503 = "CD25.03";
  private static final String LABEL_LEVEL_BASELINE = "BL";
  private static final String LABEL_LEVEL_PROFESSIONAL = "P";

  @Override
  public void run(String... args) {
    if (!repository.existsByCodeAndRulesVersion("DP-1", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("DATA PROTECTION & MANAGEMENT")
          .code("DP-1")
          .criteria(
            "The Company, when a Cloud Customer is contracting the Offering, offers a written contract under a EU/EEA/Member State law and specifically addressing GDPR requirements."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P2.1.1"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("DP-2", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("DATA PROTECTION & MANAGEMENT")
          .code("DP-2")
          .criteria(
            "The Company has defined the roles and responsibilities of each party in the delivery of the Offering."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P2.1.2"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("DP-3", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("DATA PROTECTION & MANAGEMENT")
          .code("DP-3")
          .criteria(
            "The Company has defined the technical and organizational measures in accordance with the roles and responsibilities of the parties in the delivery of the Offering, including an adequate level of detail."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P2.1.3"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("DP-4", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("DATA PROTECTION & MANAGEMENT")
          .code("DP-4")
          .criteria(
            "The Company shall not access Customer Data unless authorized by the Customer or when the access is in accordance with applicable laws governing the contract of the Offering."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P5.2.1"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("DP-5", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("DATA PROTECTION & MANAGEMENT")
          .code("DP-5")
          .criteria(
            "The Company hereby declares that the Offering is compliant with all the requirements of applicable laws and regulations concerning the protection of personal data, and specifically the General Data Protection Regulation (Regulation (EU) 2016/679)."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P1.1.5"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("CS-1", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-1")
          .criteria(
            "Organization of information security: the Company plans, implements, maintains and continuously improves the information security framework within the organisation."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.1"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("CS-2", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-2")
          .criteria(
            "Information Security Policies: the Company has a global information security policy, derived into policies and procedures regarding security requirements and to support business requirements."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.2"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("CS-3", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-3")
          .criteria(
            "Risk Management: the Company ensures that risks related to information security are properly identified, assessed, and treated, and that the residual risk is acceptable to the Company pursuant to its own criteria."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.3"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("CS-4", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-4")
          .criteria(
            "Human Resources: the Company ensures that its employees understand their responsibilities, are aware of their responsibilities regarding information security, and that the organisation’s assets are protected in the event of changes in responsibilities or termination."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.4"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("CS-5", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-5")
          .criteria(
            "Asset Management: the Company identifies the organisation’s own assets and ensures an appropriate level of protection throughout their lifecycle."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.5"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("CS-6", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-6")
          .criteria(
            "Physical Security: the Company prevents unauthorised physical access and protects premises and assets against theft, damage, loss and outage of operations."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.6"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("CS-7", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-7")
          .criteria(
            "Operational Security: the Company ensures proper and regular operation, including appropriate measures for planning and monitoring capacity, protection against malware, logging and monitoring events, and dealing with vulnerabilities, malfunctions and failures"
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.7"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("CS-8", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-8")
          .criteria(
            "Identity, Authentication and access control management: the Company limits access to information and information processing facilities."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.8"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (!repository.existsByCodeAndRulesVersion("CS-9", RULES_VERSION_CD2503)) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-9")
          .criteria(
            "Cryptography and Key management: the Company ensures appropriate and effective use of cryptography to protect the confidentiality, authenticity and integrity of information."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.9"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-10", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-10")
          .criteria(
            "Communication Security: the Company ensures the protection of information in networks and the corresponding information processing systems"
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.10"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-11", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-11")
          .criteria(
            "Portability and Interoperability: the Offering provides a means by which a Cloud Customer can obtain its stored data, and the Company provides documentation on how (and where appropriate, through documented APIs) the Cloud Customer can obtain its stored data at the end of the contractual relationship and the Company documents how the data is securely deleted from the Offering infrastructure and in what timeframe."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.11"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-12", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-12")
          .criteria(
            "Change and Configuration Management: the Company ensures that changes and configuration actions to information systems maintain an adequate security of the Offering."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.12"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-13", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-13")
          .criteria(
            "Development of Information systems: the Company ensures information security in the development cycle of the Offering."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.13"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-14", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-14")
          .criteria(
            "Procurement Management: the Company ensures the protection of information that suppliers of the Company can access and monitors the agreed services and security requirements."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.14"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-15", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-15")
          .criteria(
            "Incident Management: the Company ensures a consistent and comprehensive approach to the capture, assessment, communication and escalation of security incidents."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.15"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-16", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-16")
          .criteria(
            "Business Continuity: the Company plans, implements, maintains and tests procedures and measures for business continuity and emergency management."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.16"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-17", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-17")
          .criteria(
            "Compliance: the Company takes positive and affirmative steps to ensure compliance with legal, regulatory, self-imposed or contractual information security and compliance requirements."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.17"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-18", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-18")
          .criteria(
            "Dealing with information requests from government agencies: the Company ensures an appropriate handling of government investigation requests for legal review, information to Cloud Customers, and limitation of access to or disclosure of Cloud Customer data."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.19"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("CS-19", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-19")
          .criteria(
            "Offering security: the Company provides appropriate mechanisms for cloud customers to enable Offering security. The Company ensures that the by-default configuration of the Offering is secure."
          )
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.20"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_BASELINE)
          .build()
      );
    }
    // ---- CYBERSECURITY (Professional-tier: CS-20) ----
    if (
      !repository.existsByCodeAndRulesVersion("CS-20", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("CYBERSECURITY")
          .code("CS-20")
          .criteria(
            "User documentation: the Company provides up-to-date information on the secure configuration and known vulnerabilities of the Offering for Cloud Customers."
          )
          // Gaia-X CD25.03 criterion P3.1.18 "User documentation" (§5.5 Cybersecurity) — confirmed
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P3.1.18"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_PROFESSIONAL)
          .build()
      );
    }
    // ---- PORTABILITY (Professional-tier criteria) ----
    if (
      !repository.existsByCodeAndRulesVersion("PT-1", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("PORTABILITY")
          .code("PT-1")
          .criteria(
            "The Company implements practices for facilitating the switching of services and the porting of Cloud Customer data in a structured, commonly used and machine-readable format."
          )
          // Gaia-X CD25.03 criterion P4.1.1 (§5.6 Portability)
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P4.1.1"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_PROFESSIONAL)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("PT-2", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("PORTABILITY")
          .code("PT-2")
          .criteria(
            "The Company provides pre-contractual information to Cloud Customers, with sufficiently detailed, clear and transparent information regarding the processes of Cloud Customer data portability, technical requirements, timeframes and charges that apply in case a professional user wants to switch from the Offering to another provider or port Cloud Customer data back to its own IT systems."
          )
          // Gaia-X CD25.03 criterion P4.1.2 (§5.6 Portability)
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P4.1.2"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_PROFESSIONAL)
          .build()
      );
    }
    // ---- SUSTAINABILITY (Professional-tier criteria) ----
    if (
      !repository.existsByCodeAndRulesVersion("ST-1", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("SUSTAINABILITY")
          .code("ST-1")
          .criteria(
            "The Company provides transparency on the environmental impact of the Offering provided."
          )
          // Gaia-X CD25.03 criterion P6.1.1 (§5.8 Sustainability)
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P6.1.1"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_PROFESSIONAL)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("ST-2", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("SUSTAINABILITY")
          .code("ST-2")
          .criteria(
            "The Company ensures that the Offering meets or relies on an infrastructure which meets a high standard in energy efficiency, meeting an annual target of PUE of 1.3 in cool climates and 1.4 in warm climates."
          )
          // Gaia-X CD25.03 criterion P6.1.2 (§5.8 Sustainability)
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P6.1.2"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_PROFESSIONAL)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("ST-3", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("SUSTAINABILITY")
          .code("ST-3")
          .criteria(
            "The Company ensures that the Offering meets or relies on an infrastructure for which electricity demand will be matched by 75% renewable energy or hourly carbon-free energy by 31st December 2025, and 100% by 31st December 2030."
          )
          // Gaia-X CD25.03 criterion P6.1.3 (§5.8 Sustainability)
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P6.1.3"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_PROFESSIONAL)
          .build()
      );
    }
    if (
      !repository.existsByCodeAndRulesVersion("ST-4", RULES_VERSION_CD2503)
    ) {
      repository.save(
        CompliancesCriteriaEntity
          .builder()
          .category("SUSTAINABILITY")
          .code("ST-4")
          .criteria(
            "The Company ensures that the Service Offering meets or relies on an infrastructure Services Offering that will meet a high standard for water conservation demonstrated through the application of a location and source sensitive water usage effectiveness (WUE) target of 0.4 L/kWh in areas with water stress."
          )
          // Gaia-X CD25.03 criterion P6.1.4 (§5.8 Sustainability)
          .link(
            "https://docs.gaia-x.eu/policy-rules-committee/compliance-document/25.03/criteria_cloud_services/#P6.1.4"
          )
          .rulesVersion(RULES_VERSION_CD2503)
          .labelLevel(LABEL_LEVEL_PROFESSIONAL)
          .build()
      );
    }
  }
}
