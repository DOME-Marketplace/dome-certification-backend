# API Endpoint Documentation: /api/v1/product-offering/certificate

## Overview

This endpoint allows automated product certification creation through Machine-to-Machine (M2M) communication. It is used to request compliance certifications for digital products or services.

## Endpoint URL

```
POST /api/v1/product-offering/certificate
```

## Authentication

**Authentication Type:** Bearer Token (M2M)

```
Authorization: Bearer <m2m_token>
```

- **Description:** The endpoint accepts Machine-to-Machine (M2M) tokens issued by the DOME Marketplace verifier
- **Authorized issuer:** `verifier.dome-marketplace`
- **Validation:** The token is automatically validated by the M2M filter before processing the request

## Request Structure

### Content-Type

```
Content-Type: multipart/form-data
```

### Required Parameters

All fields are sent as `@RequestPart` in multipart format:

| Field                        | Type                      | Required | Description                                           |
| ---------------------------- | ------------------------- | -------- | ----------------------------------------------------- |
| `product_specification_id`   | String                    | ✓        | Unique identifier for the product specification       |
| `service_name`               | String                    | ✓        | Name of the service to be certified                   |
| `service_version`            | String                    | ✓        | Service version                                       |
| `organization_name`          | String                    | ✓        | Name of the requesting organization                   |
| `organization_address`       | String                    | ✓        | Complete address of the organization                  |
| `organization_country`       | String                    | ✓        | ISO country code (format: XX)                         |
| `organization_email`         | String                    | ✓        | Organization contact email (valid format)             |
| `organization_url`           | String                    | ✓        | Organization website/URL                              |
| `organization_vat_id`        | String                    | ✓        | Tax identification number (VAT ID)                    |
| `requested_compliance_level` | String                    | ✓        | Requested compliance level (only accepts: "Baseline") |
| `files`                      | List&lt;MultipartFile&gt; | ✓        | Compliance documentation files                        |

### Field Validations

- **service_name**: Cannot be empty
- **service_version**: Cannot be empty
- **organization_name**: Cannot be empty
- **organization_address**: Cannot be empty
- **organization_country**: Valid 2-character ISO code (e.g., "ES", "FR")
- **organization_email**: Valid email format
- **organization_url**: Valid URL
- **organization_vat_id**: Cannot be empty
- **requested_compliance_level**: Currently only accepts "Baseline"
- **files**: List of files in multipart format (PDF documents, images, etc.)

## Responses

### Success Response (201 Created)

```json
{
  "statusCode": 201,
  "message": null,
  "data": null
}
```

### Error Responses

#### Validation Error (400 Bad Request)

```json
{
  "statusCode": 400,
  "message": "Validation error: [field] [error description]",
  "data": null
}
```

#### Authentication Error (401 Unauthorized)

```json
{
  "statusCode": 401,
  "message": "Unauthorized - Invalid M2M token",
  "data": null
}
```

#### File Processing Error (500 Internal Server Error)

```json
{
  "statusCode": 500,
  "message": null,
  "data": null
}
```

#### Internal Server Error (500 Internal Server Error)

```json
{
  "statusCode": 500,
  "message": "Unexpected error",
  "data": null
}
```

## HTTP Status Codes

| Code    | Description                                      |
| ------- | ------------------------------------------------ |
| **201** | Created - Certification created successfully     |
| **400** | Bad Request - Validation error in submitted data |
| **401** | Unauthorized - Invalid or missing M2M token      |
| **500** | Internal Server Error - Internal server error    |

## Request Example

### cURL

```bash
curl -X POST "https://dome-certification.dome-marketplace-sbx.org/api/v1/product-offering/certificate" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "product_specification_id=PROD-2024-001" \
  -F "service_name=Payment Gateway API" \
  -F "service_version=2.1" \
  -F "organization_name=TechCorp Solutions" \
  -F "organization_address=Calle Mayor 123, 28001 Madrid" \
  -F "organization_country=ES" \
  -F "organization_email=compliance@techcorp.com" \
  -F "organization_url=https://www.techcorp.com" \
  -F "organization_vat_id=ESA12345678" \
  -F "requested_compliance_level=Baseline" \
  -F "files=@compliance_document.pdf" \
  -F "files=@technical_specification.pdf"
```

### JavaScript (Fetch)

```javascript
const formData = new FormData();
formData.append("product_specification_id", "PROD-2024-001");
formData.append("service_name", "Payment Gateway API");
formData.append("service_version", "2.1");
formData.append("organization_name", "TechCorp Solutions");
formData.append("organization_address", "Calle Mayor 123, 28001 Madrid");
formData.append("organization_country", "ES");
formData.append("organization_email", "compliance@techcorp.com");
formData.append("organization_url", "https://www.techcorp.com");
formData.append("organization_vat_id", "ESA12345678");
formData.append("requested_compliance_level", "Baseline");
formData.append("files", fileInput1.files[0]);
formData.append("files", fileInput2.files[0]);

fetch(
  "https://dome-certification.dome-marketplace-sbx.org/api/v1/product-offering/certificate",
  {
    method: "POST",
    headers: {
      Authorization: "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
    },
    body: formData,
  }
)
  .then((response) => response.json())
  .then((data) => console.log(data))
  .catch((error) => console.error("Error:", error));
```

## Additional Functionality

### Email Notification

- Once the certification is successfully created, an automatic notification email is sent
- The email is sent to the address specified in `organization_email`
- The email subject includes the service name and version
- The "email-in_progress" template is used to indicate that the certification is in progress

### File Processing

- Submitted files are processed and securely stored
- Multiple files are supported in a single request
- Files are associated with the created certification process

### Traceability

- All requests are logged for audit purposes
- The authentication type used is identified (M2M vs user JWT)
- Detailed errors are logged to facilitate debugging

## Important Notes

1. **M2M Only**: This endpoint is specifically designed for Machine-to-Machine communication
2. **Asynchronous**: Certification creation is an asynchronous process; a 201 response indicates it has started successfully
3. **Required files**: It is mandatory to send at least one documentation file
4. **Uniqueness**: The `product_specification_id` must be unique per organization and version
5. **Country format**: The country code must follow the ISO 3166-1 alpha-2 standard
