package com.dekraspain.backend.template.modules.productOffering.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

        @JsonProperty("gx:labelLevel")
        private String gxLabelLevel;

        @JsonProperty("gx:engineVersion")
        private String gxEngineVersion;

        @JsonProperty("gx:rulesVersion")
        private String gxRulesVersion;

        @JsonProperty("gx:compliantCredentials")
        private List<CompliantCredential> gxCompliantCredentials;

        @JsonProperty("gx:validatedCriteria")
        private List<String> gxValidatedCriteria;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompliantCredential {
        private String id;
        private String type;

        @JsonProperty("gx:digestSRI")
        private String gxDigestSRI;
    }
}
