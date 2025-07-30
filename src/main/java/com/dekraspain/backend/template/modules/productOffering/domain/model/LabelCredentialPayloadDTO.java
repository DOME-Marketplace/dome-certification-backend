package com.dekraspain.backend.template.modules.productOffering.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelCredentialPayloadDTO {
    private List<String> type;
    private String issuer;
    private String validFrom;
    private String validUntil;
    private CredentialSubject credentialSubject;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CredentialSubject {
        private String id;
        private String gxLabelLevel;
        private String gxEngineVersion;
        private String gxRulesVersion;
        private List<CompliantCredential> gxCompliantCredentials;
        private List<String> gxValidatedCriteria;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompliantCredential {
        private String id;
        private String type;
        private String gxDigestSRI;
    }
}
