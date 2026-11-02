# API: `POST /api/v1/product-offering/certificate`

Certification intake from the DOME Marketplace (machine-to-machine).

| | |
|---|---|
| Document version | 2.1, 2026-10-30. Replaces the September 2025 version |
| Applies to | certification backend **1.4.5** and later (the changes are marked **[1.4.5]**) |
| Audience | DOME Marketplace (FICODES) and anyone integrating with the certification tool |

## 1. Purpose and flow

The certification tool **does not fetch offerings from the Marketplace**. The Marketplace must call this endpoint for
every product offering that needs certification. Without this call the offering never reaches the certifiers.

```
Marketplace ──POST /certificate──▶ Certification tool: request created (IN_PROGRESS), email to the provider
                                        │
                                        ▼  certifier reviews the documents and decides
                        VALIDATED ──▶ email to the provider; Issuer issues a gx:LabelCredential (JWT) ──▶ Marketplace
                                        (delivered to {marketplace}/admin/uploadcertificate/{product spec URN},
                                         stored on the product specification as the Compliance:VC characteristic)
                        REJECTED  ──▶ email to the provider
```

Request states: `IN_PROGRESS` → `VALIDATED` or `REJECTED` (with the certifier's comments). A validated request becomes
`EXPIRED` when its validity ends. The provider is emailed two months before expiry and again at expiry.

**When to call:** the event that triggers the call is decided by the Marketplace. The tool accepts it at any point. The
certification gate sits on **offer publication**, so the call must happen no later than when an offer is created or
launched for the product specification.

## 2. Endpoints per environment

| Environment | Endpoint |
|---|---|
| SBX | `https://dome-certification-api.dome-marketplace-sbx.org/api/v1/product-offering/certificate` |
| DEV2 | `https://dome-certification-api.dome-marketplace-dev2.org/api/v1/product-offering/certificate` |
| PRD | `https://dome-certification-api.dome-marketplace.eu/api/v1/product-offering/certificate` |

## 3. Authentication

```
Authorization: Bearer <m2m_token>
```

The token is a JWT issued by **that environment's DOME verifier**. The tool checks:

| Check | Requirement |
|---|---|
| Algorithm | `alg` must be an ECDSA algorithm (`ES256` …). **RS256 tokens are rejected** |
| Key | the header `kid` must be present in the verifier's JWKS at `{verifier}/oidc/jwks` (keys cached 5 minutes) |
| Signature | valid for that key |
| Expiry | `exp` not passed |

Verifier JWKS per environment (the PRD value was confirmed 2026-10-30; the others are the environment defaults):

| Environment | JWKS |
|---|---|
| SBX | `https://verifier.dome-marketplace-sbx.org/oidc/jwks` |
| DEV2 | `https://verifier.dome-marketplace-dev2.org/oidc/jwks` |
| PRD | `https://verifier.dome-marketplace.eu/oidc/jwks` |

The tool does **not** currently check the `iss` or `aud` claims.

## 4. Request

`Content-Type: multipart/form-data`. Every text field is a multipart part.

| Part | Required | Description |
|---|---|---|
| `product_specification_id` | yes | Product specification ID. Sent with or without the `urn:ngsi-ld:product-specification:` prefix; the tool stores it without |
| `service_name` | yes | Service name shown to certifiers and in emails |
| `service_version` | yes | Service version |
| `organization_name` | yes | Provider organisation name |
| `organization_address` | yes | Provider address |
| `organization_country` | yes | Country (ISO 3166-1 alpha-2 recommended, e.g. `ES`) |
| `organization_email` | yes | Contact email. Receives the status notifications |
| `organization_url` | yes | Provider website |
| `organization_vat_id` | yes | Organisation identifier as used in DOME, e.g. `VATES-B12345678` |
| `requested_compliance_level` | yes | Level the provider asks for: `Baseline`, `Professional` or `Professional+`. **[1.4.5]** Variants are also accepted (`BL`, `P`, `PP`, `Professional Plus`, any case). Blank or unknown values are treated as `Baseline` and logged. Before 1.4.5, any value other than the three exact strings failed with 500 |
| `files` | **[1.4.5] no** (was required) | Zero or more compliance documents: the self-attestation and certificates, usually PDF. Repeat the part for several files. A request without documents is accepted and reaches the certifier with no evidence, so the provider will be asked for documents |

Notes:
- **Only presence is checked.** The text fields are not validated for format: no email, URL or country checks.
- **File size:** the maximum size per file and per request is set per deployment (the reference configuration is 10 MB).
  Larger uploads are rejected with 500.
- **Integrity:** the tool computes a SHA-256 of each file. That hash goes into the issued label credential
  (`gx:digestSRI`, format `sha256-<base64>`), so the document a label was based on can be verified later.
- **Repeat calls [1.4.5]** are matched on `product_specification_id` against the latest request for that product:

  | Latest request for the product | Effect of the call | Response |
  |---|---|---|
  | none | New request created | 201 |
  | `IN_PROGRESS` | **Documents added to that request.** Files identical to one already attached (same SHA-256) are skipped, so a plain retry changes nothing | 200 |
  | `VALIDATED`, `REJECTED` or `EXPIRED` | **New request** (resubmission after rejection, renewal, or adding a certificate to reach a higher level). The earlier request stays as history; a validated label stays until the new decision | 201 |

  Retries and bulk resends are therefore safe. Documents with the same filename are kept separately and shown with
  their upload time. (Before 1.4.5, every call created a new request.)

## 5. Responses

| Status | When | Body |
|---|---|---|
| **201 Created** | New request created | `{"statusCode": 201, "message": null, "data": null}` (JSON) |
| **200 OK** | **[1.4.5]** Request already in progress: documents added | `{"statusCode": 200, "message": "Added to the request in progress: N document(s) added, M identical skipped", "data": null}` (JSON) |
| **400 Bad Request** | **[1.4.5]** A required part is missing | `{"statusCode": 400, "message": "Missing required part: <part>", "data": null}` (JSON) |
| **401 Unauthorized** | Token missing, invalid or expired | **Plain text** giving the reason, e.g. `M2M token missing or invalid`, `M2M token expired: …`, `M2M token kid not found in JWKS: …`, `M2M token uses unsupported algorithm: …`, `Error fetching JWKS: …` |
| **500** | Documents could not be stored | `{"statusCode": 500, "message": null, "data": null}` |
| **500** | Any other error (e.g. not multipart, upload too large) | `{"statusCode": 500, "message": "Unexpected error", "data": null}`, or `"Error interno del servidor"` |

Treat 200 and 201 as success. On 401 or 500, fix the cause before retrying. From 1.4.5 retries are safe (see "Repeat
calls"); on earlier versions a retry after a success creates a duplicate request.

## 6. Example

```bash
curl -X POST "https://dome-certification-api.dome-marketplace-sbx.org/api/v1/product-offering/certificate" \
  -H "Authorization: Bearer <ES256 M2M token from the SBX verifier>" \
  -F "product_specification_id=urn:ngsi-ld:product-specification:7436c6e6-5d98-491d-b160-fed5bc9aeb49" \
  -F "service_name=Payment Gateway API" \
  -F "service_version=2.1" \
  -F "organization_name=TechCorp Solutions" \
  -F "organization_address=Calle Mayor 123, 28001 Madrid" \
  -F "organization_country=ES" \
  -F "organization_email=compliance@techcorp.example" \
  -F "organization_url=https://www.techcorp.example" \
  -F "organization_vat_id=VATES-A12345678" \
  -F "requested_compliance_level=Baseline" \
  -F "files=@self-attestation.pdf" \
  -F "files=@iso27001-certificate.pdf"
```

## 7. What happens after a 201

- The request appears to certifiers as `IN_PROGRESS`, and an email ("Compliance of <service_name> <service_version>
  created") goes to `organization_email`. If the email fails, the request is still created. No email is sent when
  documents are added to a request already in progress (200).
- The certifier classifies each document (certificate or self-attestation), and the tool derives the label level
  (Baseline / Professional / Professional+).
- On validation, the tool asks the DOME Issuer for a `gx:LabelCredential`. The Issuer delivers it to the Marketplace at
  `{marketplace}/admin/uploadcertificate/urn:ngsi-ld:product-specification:{id}`. A new upload **replaces** the
  previous label.
- Each intake call is logged with its product ID, and rejected calls are logged with the reason **[1.4.5]**. Give the
  certification team the product IDs when you need a call investigated.

## 8. Change log

| Version | Date | Changes |
|---|---|---|
| 2.1 | 2026-10-30 | **[1.4.5]** Repeat calls: documents added to the request in progress (200), identical files skipped; a new request only after a decision. Upload time shown per document |
| 2.0 | 2026-10-30 | Rewritten against the code. **[1.4.5]**: `files` optional; `requested_compliance_level` variants accepted; missing part → 400 (was 500); one log line per intake call, rejections logged at WARN; uploaded files stored under unique names (identically named documents from different providers no longer overwrite each other); label credential `gx:digestSRI` in SRI form and `gx:validatedCriteria` as `https://w3id.org/gaia-x/specs/cd25.03/criterion/…`. Corrections to v1: endpoint host (`dome-certification-api.…`), ES256-only tokens (the v1 example showed RS256), the format validations v1 listed don't exist, 401 is plain text, levels are not "Baseline only", and `product_specification_id` uniqueness is not enforced |
| 1.0 | 2025-09 | First version |
