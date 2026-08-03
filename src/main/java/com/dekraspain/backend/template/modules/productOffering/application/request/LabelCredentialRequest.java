package com.dekraspain.backend.template.modules.productOffering.application.request;
import java.util.List;
import com.dekraspain.backend.template.modules.productOffering.domain.model.ProductOfferingStatesDTO;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelCredentialRequest {
    @NotNull(message = "Product Offering ID is required")
    @Schema(description = "Product Offering ID", example = "6aab1234-acf4-4b44-baf9-0da25c6e558f")
    private Long poId;

    @NotNull(message = "Payload is required")
    @Schema(description = "List of compliance criteria and profiles")
    private List<ComplianceCriteriaAndProfile> payload;

    @NotNull(message = "Product Offering state data is required")
    @Schema(description = "Product Offering state data (optional)")
    private ProductOfferingStatesDTO data;

    @NotNull(message = "Credential expiration date is required")
    @Schema(description = "Credential expiration date", example = "2025-05-19T09:36:24.038Z")
    private String validUntil;

    @NotNull(message = "OIDC ID Token is required")
    @Schema(description = "OIDC ID Token of the authenticated user")
    private String idToken;

    @NotNull(message = "Response URI is required")
    @Schema(description = "Response URI for the certificate upload", example = "https://dome-marketplace-sbx.org/admin/uploadcertificate/urn:ngsi-ld:product-specification:6aab1234-acf4-4b44-baf9-0da25c6e558f")
    private String response_uri;

    @Schema(description = "Credential owner email")
    private String credential_owner_email;

    @Schema(description = "Compliance label level: BL (Baseline), P (Professional), PP (Professional Plus). Defaults to BL if omitted.", example = "P")
    private String labelLevel;

}
