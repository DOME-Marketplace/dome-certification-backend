# DOME-Certification Backend - Updates Realizados

## Data: 19 de Março de 2026

---

## Problema Original

O payload enviado para o Issuer tinha os campos Gaia-X com nomes errados (camelCase em vez de com prefixo `gx:`), resultando em credentials vazias.

**Erro identificado:**
```
gxLabelLevel        → deveria ser →  gx:labelLevel
gxEngineVersion     → deveria ser →  gx:engineVersion
gxRulesVersion      → deveria ser →  gx:rulesVersion
gxCompliantCredentials → deveria ser → gx:compliantCredentials
gxDigestSRI        → deveria ser →  gx:digestSRI
gxValidatedCriteria → deveria ser →  gx:validatedCriteria
```

---

## 1. Código Fonte - Fix @JsonProperty

**Ficheiro:** `dome-certification-backend/src/main/java/com/dekraspain/backend/template/modules/productOffering/domain/model/LabelCredentialPayloadDTO.java`

**Alteração:** Adicionar `@JsonProperty` annotations para serializar os campos com prefixo `gx:`

```java
// CredentialSubject
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

// CompliantCredential
@JsonProperty("gx:digestSRI")
private String gxDigestSRI;
```

---

## 2. Dockerfile - Base Image

**Ficheiro:** `dome-certification-backend/Dockerfile`

**Problema:** A imagem `openjdk:17-jdk-alpine` foi descontinuada.

**Solução:** Usar `eclipse-temurin:17-jre`

```dockerfile
# ANTES
FROM openjdk:17-jdk-alpine

# DEPOIS
FROM eclipse-temurin:17-jre
```

---

## 3. Build e Push da Imagem Docker

```bash
# Build para platform AMD64 (Mac M1/M2)
docker build --platform linux/amd64 -t bfeitaisuw/dome-compliance-backend:sbx-1.3.12 .

# Push para Docker Hub
docker push bfeitaisuw/dome-compliance-backend:sbx-1.3.12
```

**Imagem:** `bfeitaisuw/dome-compliance-backend:sbx-1.3.12`
**Tamanho:** 158MB
**Arquitetura:** linux/amd64

---

## 4. Deployment - GitOps

### certification-backend.yaml

**Problema:** A variável `POSTGRES_PASSWORD` não é reconhecida pelo Spring Boot.

**Solução:** Usar `SPRING_DATASOURCE_PASSWORD`

```yaml
# ANTES
- name: POSTGRES_PASSWORD
  valueFrom:
    secretKeyRef:
      name: dekra-postgres-secret
      key: postgres-password

# DEPOIS
- name: SPRING_DATASOURCE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: dekra-postgres-secret
      key: postgres-password
```

---

## 5. ConfigMap - Variáveis Adicionais

### certification-backend-config.yaml

**Adições:** Variáveis necessárias para o código do branch `develop` que faltavam:

```yaml
JWT_OAUTH_ISSUER: https://issuer.dome-marketplace-sbx.org
EMAIL_ADDRESS: certification@dome-marketplace.eu
```

---

## 6. Alterações Manuais no Cluster (Temporárias)

As seguintes alterações foram feitas diretamente no cluster via kubectl e serão revertidas quando o ArgoCD fizer sync:

```bash
# Adicionar SPRING_DATASOURCE_PASSWORD ao ConfigMap
kubectl patch configmap certification-backend-config -n dome-certification \
  --type merge -p '{"data":{"SPRING_DATASOURCE_PASSWORD":"root"}}'

# Adicionar EMAIL_ADDRESS ao ConfigMap
kubectl patch configmap certification-backend-config -n dome-certification \
  --type merge -p '{"data":{"EMAIL_ADDRESS":"certification@dome-marketplace.eu"}}'

# Adicionar JWT_OAUTH_ISSUER ao ConfigMap
kubectl patch configmap certification-backend-config -n dome-certification \
  --type merge -p '{"data":{"JWT_OAUTH_ISSUER":"https://issuer.dome-marketplace-sbx.org"}}'
```

---

## Payload Verificado (Funcionando)

O payload enviado para o issuer está correto:

```json
{
  "email" : "apereira@ubiwhere.com",
  "response_uri" : "https://dome-marketplace-sbx.org/admin/uploadcertificate/urn:ngsi-ld:product-specification:Product 1",
  "schema" : "gx:LabelCredential",
  "payload" : {
    "type" : [ "VerifiableCredential", "gx:LabelCredential" ],
    "issuer" : "did:elsi:VATPT-508245567",
    "validFrom" : "2026-03-19T11:14:10.716Z",
    "validUntil" : "2027-03-19T11:14:04.770Z",
    "credentialSubject" : {
      "id" : "urn:ngsi-ld:product-specification:Product 1",
      "gx:labelLevel" : "BL",
      "gx:engineVersion" : "1.2.1",
      "gx:rulesVersion" : "CD25.03",
      "gx:compliantCredentials" : [ ... ],
      "gx:validatedCriteria" : [ ... ]
    }
  },
  "operation_mode" : "S",
  "format" : "jwt_vc_json"
}
```

---

## Ficheiros Alterados

| Ficheiro | Ação |
|----------|------|
| `LabelCredentialPayloadDTO.java` | Adicionado @JsonProperty |
| `Dockerfile` | Alterado base image |
| `certification-backend.yaml` | ALTERADO: POSTGRES_PASSWORD → SPRING_DATASOURCE_PASSWORD |
| `certification-backend-config.yaml` | ADICIONADO: JWT_OAUTH_ISSUER, EMAIL_ADDRESS |

---

## Para Aplicar em Produção

1. Commit e push das alterações para a branch principal do gitops
2. Criar PR da branch `SBX_DomeCertificationBackend` para `main`
3. Merge do PR
4. ArgoCD sincroniza automaticamente

### Comandos:

```bash
cd /Users/brunofeitais/GIT_CLONES/dome-gitops
git add ionos_sbx/dome-certification/backend/
git commit -m "fix: Update certification-backend with gx: field names fix"
git push

# Criar PR via GitHub CLI ou website
gh pr create --base main --head SBX_DomeCertificationBackend
```

---

## Notas Adicionais

- O código de Label Credential (novo) está no branch `develop`, não no `production`
- A imagem `openjdk:17-jdk-alpine` foi descontinuada - usar `eclipse-temurin:17-jre`
- O issuer retorna 201 CREATED mas com response body null (problema do issuer, não do backend)
- O email está a falhar com "User not authenticated" (problema de configuração SMTP)

---

## Erros Resolvidos

1. ✅ Campos `gx:*` com nomes corretos via @JsonProperty
2. ✅ Variável `SPRING_DATASOURCE_PASSWORD` em vez de `POSTGRES_PASSWORD`
3. ✅ Variável `JWT_OAUTH_ISSUER` adicionada
4. ✅ Variável `EMAIL_ADDRESS` adicionada
5. ✅ Base image corrigida para `eclipse-temurin:17-jre`
6. ✅ JWT Authentication Filter - ignorar tokens ES256/RS do issuer/verifier

---

## 7. Fix JWT Authentication Filter

### Problema

Erro nos logs:
```
io.jsonwebtoken.UnsupportedJwtException: The parsed JWT indicates it was signed with the 'ES256' signature algorithm, but the provided javax.crypto.spec.SecretKeySpec key may not be used to verify ES256 signatures.
```

**Causa:** O `JwtAuthenticationFilter` recebia tokens ES256 (do issuer/verifier) e tentava verificá-los como tokens internos HMAC.

### Fluxo do Erro

```
1. Request com JWT (do issuer/verifier - ES256)
       ↓
2. JwtAuthenticationFilter extrai o JWT
       ↓
3. isTokenValid() → getClaim() → getAllClaims()
       ↓
4. getAllClaims() tenta verificar com HMAC (jwt-secret-key)
       ↓
5. JWT é ES256 → ERRO
```

### Solução

O filtro agora deteta tokens externos (ES256, RS256, etc.) e ignora-os - não são para autenticação interna.

**Ficheiro:** `JwtAuthenticationFilter.java`

**Alterações:**

1. Novo método `isExternalToken()` para detetar algoritmos externos:
```java
private boolean isExternalToken(String token) {
    try {
        String[] parts = token.split("\\.");
        if (parts.length < 1) return false;
        
        String headerJson = new String(Base64.getDecoder().decode(parts[0]));
        
        // ES256, RS256, RS384, RS512, ES384, ES512 são tokens externos
        return headerJson.contains("ES256") || 
               headerJson.contains("RS256") ||
               headerJson.contains("RS384") ||
               headerJson.contains("RS512") ||
               headerJson.contains("ES384") ||
               headerJson.contains("ES512");
    } catch (Exception e) {
        return false;
    }
}
```

2. No `doFilterInternal()`, ignorar tokens externos:
```java
// Check if token is ES256 (from issuer/verifier) - skip verification
if (isExternalToken(token)) {
    log.debug("JwtAuthenticationFilter: skipping ES256/RS token from issuer/verifier");
    filterChain.doFilter(request, response);
    return;
}
```

3. Catch exceptions ao extrair userId:
```java
try {
    userId = UUID.fromString(jwtService.getUserIdFromToken(token));
} catch (Exception e) {
    log.warn("JwtAuthenticationFilter: could not extract userId from token: {}", e.getMessage());
    filterChain.doFilter(request, response);
    return;
}
```

### Imagem Resultante

```
bfeitaisuw/dome-compliance-backend:sbx-1.3.12 (ou sbx-1.3.14)
```

---

## Resumo Completo dos Fixes

| # | Problema | Solução | Ficheiro |
|---|----------|---------|----------|
| 1 | Campos `gx:*` sem prefixo | Adicionar `@JsonProperty` | `LabelCredentialPayloadDTO.java` |
| 2 | Base image descontinuada | Usar `eclipse-temurin:17-jre` | `Dockerfile` |
| 3 | `POSTGRES_PASSWORD` não reconhecido | Usar `SPRING_DATASOURCE_PASSWORD` | `certification-backend.yaml` |
| 4 | Variáveis em falta | Adicionar `JWT_OAUTH_ISSUER`, `EMAIL_ADDRESS` | `certification-backend-config.yaml` |
| 5 | Tokens ES256 causam erro no filtro | Ignorar tokens externos no filtro | `JwtAuthenticationFilter.java` |

---

## Notas de Implementação

- O código de Label Credential está no branch `develop`, não no `production`
- Os tokens ES256/RS256 são do issuer/verifier e não devem ser verificados localmente
- O issuer retorna 201 CREATED mas com response body null (problema do issuer)
- O email está a falhar com "User not authenticated" (problema de configuração SMTP)
