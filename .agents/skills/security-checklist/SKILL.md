---
name: security-checklist
description: Verify security vulnerabilities — hardcoded secrets, SQL injection, JWT validation, API key masking, sensitive logging, and authorization checks. Run before merging any auth or API-related changes.
---

# Security Checklist

## Verification Items

### 1. Hardcoded Secrets
- [ ] No API Key, Secret, Password in code?
- [ ] Using environment variables or config files?

Verification commands:
```bash
# Basic search in Kotlin files
grep -r "password.*=.*\"" --include="*.kt"
grep -r "secret.*=.*\"" --include="*.kt"
grep -r "apiKey.*=.*\"" --include="*.kt"

# Check YAML/Properties files
grep -r "password\|secret\|apiKey" --include="*.yml" --include="*.yaml" --include="*.properties"

# Check for base64 encoded strings (potential secrets)
grep -rE "['\"]([A-Za-z0-9+/]{40,}={0,2})['\"]" --include="*.kt"
```

**Limitations:**
- May miss secrets encoded in base64 or other formats
- May not detect secrets loaded from external sources at runtime
- May not find secrets in configuration files outside the codebase
- Manual review is still recommended for sensitive areas

### 2. SQL Injection
- [ ] Using PreparedStatement or JPA/QueryDSL?
- [ ] Not concatenating SQL strings directly?

### 3. JWT Verification
- [ ] Verifying JWT signature?
- [ ] Checking expiration time?
- [ ] Validating claims?

### 4. API Key Security
- [ ] Masking API Key in responses?
- [ ] Encrypting API Key when storing?

### 5. Logging
- [ ] Not logging sensitive info (password, token, etc.)?
- [ ] Appropriate log level?

### 6. Authorization
- [ ] Using `@PreAuthorize` or Security Filter for auth-required endpoints?
- [ ] Verifying access to own resources only?

## References

Locate reference files at runtime:

```bash
find . -name "ApiKeyService.kt" ! -path "*/build/*"
find . -type d -name "security" -path "*/main/*" ! -path "*/build/*"
```
