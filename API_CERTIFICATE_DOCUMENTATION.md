# API: `POST /api/v1/product-offering/certificate`

Alta de solicitudes de certificación desde el DOME Marketplace (máquina a máquina).

| | |
|---|---|
| Versión del documento | 2.1, 2026-10-30. Sustituye a la versión de septiembre de 2025 |
| Aplica a | backend de certificación **1.4.5** y posteriores (los cambios se marcan con **[1.4.5]**) |
| Destinatarios | DOME Marketplace (FICODES) y cualquier integración con la herramienta de certificación |

## 1. Propósito y flujo

La herramienta de certificación **no consulta el Marketplace**. El Marketplace debe llamar a este endpoint para cada
oferta de producto que necesite certificación. Sin esta llamada, la oferta nunca llega a los certificadores.

```
Marketplace ──POST /certificate──▶ Herramienta: solicitud creada (IN_PROGRESS), email al proveedor
                                        │
                                        ▼  el certificador revisa los documentos y decide
                        VALIDATED ──▶ email al proveedor; el Issuer emite una gx:LabelCredential (JWT) ──▶ Marketplace
                                        (entregada en {marketplace}/admin/uploadcertificate/{URN de la especificación},
                                         guardada en la especificación como la característica Compliance:VC)
                        REJECTED  ──▶ email al proveedor
```

Estados: `IN_PROGRESS` → `VALIDATED` o `REJECTED` (con los comentarios del certificador). Una solicitud validada pasa a
`EXPIRED` cuando termina su validez. El proveedor recibe un email dos meses antes de la expiración y otro al expirar.

**Cuándo llamar:** el evento que dispara la llamada lo decide el Marketplace. La herramienta la acepta en cualquier
momento. El control de certificación está en la **publicación de la oferta**, así que la llamada debe hacerse, como muy
tarde, al crear o lanzar una oferta para la especificación de producto.

## 2. Endpoints por entorno

| Entorno | Endpoint |
|---|---|
| SBX | `https://dome-certification-api.dome-marketplace-sbx.org/api/v1/product-offering/certificate` |
| DEV2 | `https://dome-certification-api.dome-marketplace-dev2.org/api/v1/product-offering/certificate` |
| PRD | `https://dome-certification-api.dome-marketplace.eu/api/v1/product-offering/certificate` |

## 3. Autenticación

```
Authorization: Bearer <m2m_token>
```

El token es un JWT emitido por **el verificador DOME de ese entorno**. La herramienta comprueba:

| Comprobación | Requisito |
|---|---|
| Algoritmo | `alg` debe ser ECDSA (`ES256` …). **Los tokens RS256 se rechazan** |
| Clave | el `kid` de la cabecera debe estar en el JWKS del verificador, `{verifier}/oidc/jwks` (claves en caché 5 minutos) |
| Firma | válida para esa clave |
| Expiración | `exp` no vencido |

JWKS del verificador por entorno (PRD confirmado el 2026-10-30; los demás son los valores por defecto del entorno):

| Entorno | JWKS |
|---|---|
| SBX | `https://verifier.dome-marketplace-sbx.org/oidc/jwks` |
| DEV2 | `https://verifier.dome-marketplace-dev2.org/oidc/jwks` |
| PRD | `https://verifier.dome-marketplace.eu/oidc/jwks` |

Actualmente la herramienta **no** comprueba los claims `iss` ni `aud`.

## 4. Petición

`Content-Type: multipart/form-data`. Cada campo de texto es una parte multipart.

| Parte | Obligatoria | Descripción |
|---|---|---|
| `product_specification_id` | sí | ID de la especificación de producto. Se acepta con o sin el prefijo `urn:ngsi-ld:product-specification:`; la herramienta lo guarda sin él |
| `service_name` | sí | Nombre del servicio, visible para los certificadores y en los emails |
| `service_version` | sí | Versión del servicio |
| `organization_name` | sí | Nombre de la organización proveedora |
| `organization_address` | sí | Dirección del proveedor |
| `organization_country` | sí | País (se recomienda ISO 3166-1 alfa-2, p. ej. `ES`) |
| `organization_email` | sí | Email de contacto. Recibe las notificaciones de estado |
| `organization_url` | sí | Web del proveedor |
| `organization_vat_id` | sí | Identificador de la organización tal como se usa en DOME, p. ej. `VATES-B12345678` |
| `requested_compliance_level` | sí | Nivel que solicita el proveedor: `Baseline`, `Professional` o `Professional+`. **[1.4.5]** También se aceptan variantes (`BL`, `P`, `PP`, `Professional Plus`, sin distinguir mayúsculas). Un valor vacío o desconocido se trata como `Baseline` y se registra en el log. Antes de 1.4.5, cualquier valor distinto de los tres exactos daba error 500 |
| `files` | **[1.4.5] no** (antes obligatoria) | Cero o más documentos de cumplimiento: la autodeclaración y los certificados, normalmente PDF. Repetir la parte para varios ficheros. Una petición sin documentos se acepta y llega al certificador sin evidencias, así que se pedirán documentos al proveedor |

Notas:
- **Solo se comprueba la presencia.** No se valida el formato de los campos de texto: ni email, ni URL, ni país.
- **Tamaño de ficheros:** el máximo por fichero y por petición se configura en cada despliegue (la configuración de
  referencia es 10 MB). Las subidas mayores se rechazan con 500.
- **Integridad:** la herramienta calcula un SHA-256 de cada fichero. Ese hash va en la credencial emitida
  (`gx:digestSRI`, formato `sha256-<base64>`), así que se puede verificar después en qué documento se basó la etiqueta.
- **Llamadas repetidas [1.4.5]:** se comparan por `product_specification_id` con la última solicitud de ese producto:

  | Última solicitud del producto | Efecto de la llamada | Respuesta |
  |---|---|---|
  | ninguna | Se crea una nueva solicitud | 201 |
  | `IN_PROGRESS` | **Los documentos se añaden a esa solicitud.** Los ficheros idénticos a uno ya adjunto (mismo SHA-256) se omiten, así que un reintento simple no cambia nada | 200 |
  | `VALIDATED`, `REJECTED` o `EXPIRED` | **Nueva solicitud** (reenvío tras un rechazo, renovación o añadir un certificado para subir de nivel). La solicitud anterior queda como historial; una etiqueta validada se mantiene hasta la nueva decisión | 201 |

  Por tanto, los reintentos y los reenvíos masivos son seguros. Los documentos con el mismo nombre se guardan por
  separado y se muestran con su hora de subida. (Antes de 1.4.5, cada llamada creaba una nueva solicitud.)

## 5. Respuestas

| Estado | Cuándo | Cuerpo |
|---|---|---|
| **201 Created** | Nueva solicitud creada | `{"statusCode": 201, "message": null, "data": null}` (JSON) |
| **200 OK** | **[1.4.5]** Solicitud ya en curso: documentos añadidos | `{"statusCode": 200, "message": "Added to the request in progress: N document(s) added, M identical skipped", "data": null}` (JSON) |
| **400 Bad Request** | **[1.4.5]** Falta una parte obligatoria | `{"statusCode": 400, "message": "Missing required part: <parte>", "data": null}` (JSON) |
| **401 Unauthorized** | Token ausente, inválido o caducado | **Texto plano** con el motivo, p. ej. `M2M token missing or invalid`, `M2M token expired: …`, `M2M token kid not found in JWKS: …`, `M2M token uses unsupported algorithm: …`, `Error fetching JWKS: …` |
| **500** | No se pudieron guardar los documentos | `{"statusCode": 500, "message": null, "data": null}` |
| **500** | Cualquier otro error (p. ej. no es multipart, subida demasiado grande) | `{"statusCode": 500, "message": "Unexpected error", "data": null}`, o `"Error interno del servidor"` |

Considerad 200 y 201 como éxito. Ante un 401 o 500, corregid la causa antes de reintentar. Desde 1.4.5 los reintentos
son seguros (ver "Llamadas repetidas"); en versiones anteriores, reintentar tras un éxito duplica la solicitud.

## 6. Ejemplo

```bash
curl -X POST "https://dome-certification-api.dome-marketplace-sbx.org/api/v1/product-offering/certificate" \
  -H "Authorization: Bearer <token M2M ES256 del verificador SBX>" \
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
  -F "files=@autodeclaracion.pdf" \
  -F "files=@certificado-iso27001.pdf"
```

## 7. Qué ocurre tras un 201

- La solicitud aparece para los certificadores como `IN_PROGRESS`, y se envía un email ("Compliance of <service_name>
  <service_version> created") a `organization_email`. Si el email falla, la solicitud se crea igualmente. No se envía
  email cuando los documentos se añaden a una solicitud ya en curso (200).
- El certificador clasifica cada documento (certificado o autodeclaración) y la herramienta deriva el nivel de la
  etiqueta (Baseline / Professional / Professional+).
- Al validar, la herramienta pide al DOME Issuer una `gx:LabelCredential`. El Issuer la entrega al Marketplace en
  `{marketplace}/admin/uploadcertificate/urn:ngsi-ld:product-specification:{id}`. Una nueva subida **sustituye** a la
  etiqueta anterior.
- Cada llamada se registra en el log con su ID de producto, y las rechazadas con el motivo **[1.4.5]**. Si necesitáis
  investigar una llamada, pasad los IDs de producto al equipo de certificación.

## 8. Historial de cambios

| Versión | Fecha | Cambios |
|---|---|---|
| 2.1 | 2026-10-30 | **[1.4.5]** Llamadas repetidas: documentos añadidos a la solicitud en curso (200), ficheros idénticos omitidos; nueva solicitud solo tras una decisión. Hora de subida visible por documento |
| 2.0 | 2026-10-30 | Reescrito según el código. **[1.4.5]**: `files` opcional; variantes de `requested_compliance_level` aceptadas; parte ausente → 400 (antes 500); una línea de log por llamada, rechazos registrados como WARN; ficheros guardados con nombre único (los documentos con el mismo nombre de distintos proveedores ya no se sobrescriben); en la credencial, `gx:digestSRI` en formato SRI y `gx:validatedCriteria` como `https://w3id.org/gaia-x/specs/cd25.03/criterion/…`. Correcciones respecto a v1: host del endpoint (`dome-certification-api.…`), solo tokens ES256 (el ejemplo de v1 usaba RS256), las validaciones de formato que listaba v1 no existen, el 401 es texto plano, los niveles no son "solo Baseline" y no se exige unicidad de `product_specification_id` |
| 1.0 | 2025-09 | Primera versión |
