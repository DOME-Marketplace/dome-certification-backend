# API Endpoint Documentation: /api/v1/product-offering/certificate

## Descripción General

Este endpoint permite crear certificaciones de productos de manera automatizada (Machine-to-Machine). Es utilizado para solicitar certificaciones de cumplimiento normativo de productos o servicios digitales.

## URL del Endpoint

```
POST /api/v1/product-offering/certificate
```

## Autenticación

**Tipo de Autenticación:** Bearer Token (M2M)

```
Authorization: Bearer <m2m_token>
```

- **Descripción:** El endpoint acepta tokens Machine-to-Machine (M2M) emitidos por el verifier de DOME Marketplace
- **Emisor autorizado:** `verifier.dome-marketplace`
- **Validación:** El token es validado automáticamente por el filtro M2M antes de procesar la petición

## Estructura de la Petición

### Content-Type

```
Content-Type: multipart/form-data
```

### Parámetros Requeridos

Todos los campos son enviados como `@RequestPart` en formato multipart:

| Campo                         | Tipo                      | Obligatorio | Descripción                                                |
| ----------------------------- | ------------------------- | ----------- | ---------------------------------------------------------- |
| `product_specification_id`    | String                    | ✓           | Identificador único de la especificación del producto      |
| `service_name`                | String                    | ✓           | Nombre del servicio a certificar                           |
| `service_version`             | String                    | ✓           | Versión del servicio                                       |
| `organization_name`           | String                    | ✓           | Nombre de la organización solicitante                      |
| `organization_address`        | String                    | ✓           | Dirección completa de la organización                      |
| `organization_country`        | String                    | ✓           | Código ISO de país (formato: XX)                           |
| `organization_email`          | String                    | ✓           | Email de contacto de la organización (formato válido)      |
| `organization_url`            | String                    | ✓           | URL/sitio web de la organización                           |
| `organization_vat_id`         | String                    | ✓           | Número de identificación fiscal (VAT ID)                   |
| `requested_compliances_level` | String                    | ✓           | Nivel de cumplimiento solicitado (solo acepta: "Baseline") |
| `files`                       | List&lt;MultipartFile&gt; | ✓           | Archivos de documentación de cumplimiento                  |

### Validaciones de Campos

- **service_name**: No puede estar vacío
- **service_version**: No puede estar vacío
- **organization_name**: No puede estar vacío
- **organization_address**: No puede estar vacío
- **organization_country**: Código ISO válido de 2 caracteres (ej: "ES", "FR")
- **organization_email**: Formato de email válido
- **organization_url**: URL válida
- **organization_vat_id**: No puede estar vacío
- **requested_compliances_level**: Por el momento solo aceptamos "Baseline"
- **files**: Lista de archivos en formato multipart (documentos PDF, imágenes, etc.)

## Respuestas

### Respuesta Exitosa (201 Created)

```json
{
  "statusCode": 201,
  "message": null,
  "data": null
}
```

### Respuestas de Error

#### Error de Validación (400 Bad Request)

```json
{
  "statusCode": 400,
  "message": "Validation error: [campo] [descripción del error]",
  "data": null
}
```

#### Error de Autenticación (401 Unauthorized)

```json
{
  "statusCode": 401,
  "message": "Unauthorized - Invalid M2M token",
  "data": null
}
```

#### Error de Procesamiento de Archivos (500 Internal Server Error)

```json
{
  "statusCode": 500,
  "message": null,
  "data": null
}
```

#### Error Interno del Servidor (500 Internal Server Error)

```json
{
  "statusCode": 500,
  "message": "Unexpected error",
  "data": null
}
```

## Códigos de Estado HTTP

| Código  | Descripción                                             |
| ------- | ------------------------------------------------------- |
| **201** | Created - Certificación creada exitosamente             |
| **400** | Bad Request - Error de validación en los datos enviados |
| **401** | Unauthorized - Token M2M inválido o ausente             |
| **500** | Internal Server Error - Error interno del servidor      |

## Ejemplo de Petición

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
  -F "requested_compliances_level=Baseline" \
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
formData.append("requested_compliances_level", "Baseline");
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

## Funcionalidad Adicional

### Notificación por Email

- Una vez creada la certificación exitosamente, se envía automáticamente un email de notificación
- El email se envía a la dirección especificada en `organization_email`
- El asunto del email incluye el nombre y versión del servicio
- Se utiliza la plantilla "email-in_progress" para indicar que la certificación está en proceso

### Procesamiento de Archivos

- Los archivos enviados son procesados y almacenados de forma segura
- Se admiten múltiples archivos en una sola petición
- Los archivos son asociados al proceso de certificación creado

### Trazabilidad

- Todas las peticiones son registradas en logs para auditoría
- Se identifica el tipo de autenticación utilizada (M2M vs JWT de usuario)
- Se registran errores detallados para facilitar el debugging

## Notas Importantes

1. **Solo M2M**: Este endpoint está diseñado específicamente para comunicación Machine-to-Machine
2. **Asíncrono**: La creación de la certificación es un proceso asíncrono; la respuesta 201 indica que se ha iniciado correctamente
3. **Archivos requeridos**: Es obligatorio enviar al menos un archivo de documentación
4. **Unicidad**: El `product_specification_id` debe ser único por organización y version
5. **Formato de país**: El código de país debe seguir el estándar ISO 3166-1 alpha-2
