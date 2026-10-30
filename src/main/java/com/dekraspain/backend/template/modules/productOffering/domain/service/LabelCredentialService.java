package com.dekraspain.backend.template.modules.productOffering.domain.service;

import com.dekraspain.backend.template.modules.productOffering.application.request.ComplianceCriteriaAndProfile;
import com.dekraspain.backend.template.modules.productOffering.domain.model.LabelCredentialPayloadDTO;
import com.dekraspain.backend.template.modules.productOffering.persistence.entity.ProductOfferingEntity;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ComplianceProfileRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.CompliancesCriteriaRepository;
import com.dekraspain.backend.template.modules.productOffering.persistence.jpa.ProductOfferingRepository;
import com.dekraspain.backend.template.modules.user.persistence.entity.UserEntity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

//     const payload: {
//     schema: string;
//     operation_mode: string;
//     format: string;
//     response_uri: string;
//     payload: {
//         type: string[];
//         credentialSubject: {
//             company: {
//                 address: string;
//                 commonName: string;
//                 country: string;
//                 email: string;
//                 id: string;
//                 organization: string;
//             };
//             compliance: IssuerCompliance[];
//             product: {
//                 ...;
//             };
//         };
//         validFrom: string;
//         validUntil: string;
//     };
// }

@Service
@RequiredArgsConstructor
public class LabelCredentialService {



    private final ProductOfferingRepository productOfferingRepository;
    private final CompliancesCriteriaRepository compliancesCriteriaRepository;
    private final ComplianceProfileRepository complianceProfileRepository;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    public LabelCredentialPayloadDTO generateLabelCredentialPayload(Long poId, List<ComplianceCriteriaAndProfile> payload, UserEntity user, String validUntil, String labelLevel) {
        ProductOfferingEntity po = productOfferingRepository.findById(poId)
                .orElseThrow(() -> new IllegalArgumentException("ProductOffering not found"));

        // Defensive default: an absent/blank level must never issue a null-level credential.
        // Matches the LabelCredentialRequest schema ("Defaults to BL if omitted").
        String effectiveLabelLevel = (labelLevel == null || labelLevel.isBlank()) ? "BL" : labelLevel;

        List<LabelCredentialPayloadDTO.CompliantCredential> compliantCredentials = new ArrayList<>();
        List<String> validatedCriteria = new ArrayList<>();
        final String[] rulesVersion = {null};

        payload.forEach(item -> {
            var criteriaOpt = compliancesCriteriaRepository.findById(item.getComplianceCriteriaId());
            var profileOpt = complianceProfileRepository.findById(item.getComplianceProfileId());
            if (criteriaOpt.isEmpty() || profileOpt.isEmpty()) return;
            var criteria = criteriaOpt.get();
            var profile = profileOpt.get();
            if (rulesVersion[0] == null && criteria.getRulesVersion() != null) {
                rulesVersion[0] = criteria.getRulesVersion();
            }
            compliantCredentials.add(LabelCredentialPayloadDTO.CompliantCredential.builder()
                    .id("urn:criteria:" + criteria.getCode().toLowerCase())
                    .type(mapCategoryToType(criteria.getCategory()))
                    .gxDigestSRI(toDigestSri(profile.getHash()))
                    .build());
            if (criteria.getLink() != null && !criteria.getLink().isEmpty()) {
                validatedCriteria.add(toCriterionUri(criteria.getLink(), criteria.getRulesVersion()));
            }
        });

        LabelCredentialPayloadDTO.CredentialSubject credentialSubject = LabelCredentialPayloadDTO.CredentialSubject.builder()
                .id("urn:ngsi-ld:product-specification:" + po.getId_PO())
                .gxLabelLevel(effectiveLabelLevel)
                .gxEngineVersion(appVersion)
                .gxRulesVersion(rulesVersion[0] != null ? rulesVersion[0] : "CD25.03")
                .gxCompliantCredentials(compliantCredentials)
                .gxValidatedCriteria(validatedCriteria)
                .build();

        

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String validFrom = ZonedDateTime.now(ZoneOffset.UTC).format(formatter);
        return LabelCredentialPayloadDTO.builder()
                .type(List.of("VerifiableCredential", "gx:LabelCredential"))
                .issuer("did:elsi:" + user.getOrganization_id())
                .validFrom(validFrom)
                .validUntil(validUntil)
                .credentialSubject(credentialSubject)
                .build();
    }

    // Gaia-X criterion URI in the form the DOME label credentials already live in PRD use
    // (https://w3id.org/gaia-x/specs/cd25.03/criterion/P2.1.1). The seeded link points at the
    // human-readable docs page, whose #anchor is the criterion id. Links without an anchor pass through.
    static String toCriterionUri(String link, String rulesVersion) {
        if (link == null) return null;
        int anchor = link.lastIndexOf('#');
        if (anchor < 0 || anchor == link.length() - 1) return link;
        String version = (rulesVersion == null || rulesVersion.isBlank()) ? "CD25.03" : rulesVersion;
        return "https://w3id.org/gaia-x/specs/" + version.toLowerCase(Locale.ROOT)
                + "/criterion/" + link.substring(anchor + 1);
    }

    // compliance_profile.hash holds the hex SHA-256 of the uploaded file; gx:digestSRI uses the
    // Subresource Integrity form "sha256-<base64>". Values already in SRI form, or not a hex
    // SHA-256, pass through unchanged.
    static String toDigestSri(String hash) {
        if (hash == null || hash.startsWith("sha256-") || !hash.matches("[0-9a-fA-F]{64}")) return hash;
        return "sha256-" + Base64.getEncoder().encodeToString(HexFormat.of().parseHex(hash));
    }

    private String mapCategoryToType(String category) {
        if (category == null) return "gx:Other";
        return switch (category.trim().toUpperCase()) {
            case "DATA PROTECTION & MANAGEMENT" -> "gx:DataProtection";
            case "CYBERSECURITY" -> "gx:Cybersecurity";
            case "PORTABILITY" -> "gx:Portability";
            case "SUSTAINABILITY" -> "gx:Sustainability";
            case "EUROPEAN CONTROL" -> "gx:EuropeanControl";
            default -> "gx:Other";
        };
    }
}
