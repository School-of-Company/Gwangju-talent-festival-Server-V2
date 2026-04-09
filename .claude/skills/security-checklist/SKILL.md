---
name: security-checklist
description: Verify security vulnerabilities — hardcoded secrets, SQL injection, JWT validation, blacklist enforcement, sensitive logging, and authorization checks. Run before merging any auth or API-related changes.
allowed-tools: Bash(git *:*), Bash(grep *:*), Read, Glob, Grep
---

## Step 1 — Gather Changed Files

```bash
git diff develop...HEAD --name-only 2>/dev/null || git diff HEAD~5...HEAD --name-only
```

Read each changed `.java` file with the Read tool for detailed analysis.

---

## Step 2 — Run Checklist

### 1. Hardcoded Secrets
- [ ] No API key, password, or secret value hardcoded in `.java` files?
- [ ] No secrets in `application.yml` — using `${ENV_VAR}` placeholders?
- [ ] No base64-encoded secret strings embedded in code?

Search commands:
```bash
grep -rn "password\s*=\s*\"" src/main/java --include="*.java"
grep -rn "secret\s*=\s*\"" src/main/java --include="*.java"
grep -rn "apiKey\s*=\s*\"" src/main/java --include="*.java"
grep -rE "['\"]([A-Za-z0-9+/]{40,}={0,2})['\"]" src/main/java --include="*.java"
```

### 2. SQL Injection
- [ ] All queries use JPA, QueryDSL, or `@Query` with named parameters — no string concatenation?
- [ ] No native queries built with user input directly?

### 3. JWT Validation (see `JwtProvider`, `JwtFilter`)
- [ ] Signature is validated via `jwtProvider.validateToken()`?
- [ ] Expiration is checked before processing claims?
- [ ] Token type (`isAccessToken`) is verified before granting access?
- [ ] JTI is extracted and checked against `TokenBlacklistRepository` on every request?
- [ ] Refresh token is deleted from Redis on logout?

### 4. Blacklist Enforcement (see `JwtFilter`, `LogoutServiceImpl`)
- [ ] Blacklist check happens in `JwtFilter` before `SecurityContextHolder` is populated?
- [ ] JTI is stored with TTL equal to remaining token expiry (no permanent entries)?
- [ ] Redis failure is handled gracefully (fail-open with error log)?

### 5. Sensitive Data in Logs
- [ ] No password, token, or JTI logged at any level?
- [ ] No personal information (phone number, name) in debug/error logs?
- [ ] Log level is appropriate — no excessive `log.debug` left in production paths?

### 6. Authorization
- [ ] All protected endpoints require authentication via `SecurityConfig`?
- [ ] Resource ownership is verified — users can only access their own data?
- [ ] Admin-only endpoints are restricted by `Role` check or `@PreAuthorize`?
- [ ] SSE endpoints (`/seat/changes`, `/judge/sse`) are properly secured?

### 7. Input Validation
- [ ] All request DTOs use `@Valid` in controller method parameters?
- [ ] Constraints (`@NotBlank`, `@NotNull`, etc.) are applied on record fields?
- [ ] No user-controlled values passed directly to file paths or external commands?

### 8. External API Keys (CoolSMS, Google Sheets)
- [ ] API keys loaded from properties/environment — not hardcoded?
- [ ] API keys not returned in any response DTO?
- [ ] API key values not printed in logs?

---

## Step 3 — Report

Output each item as:
- ✓ Pass
- ⚠ Warning (recommendation)
- ✗ Error (must fix)

Group by category, then summarize:

```
Total {n} items checked
✓ {p} passed / ⚠ {w} warnings / ✗ {e} errors

Issues found:
- ✗ [file] : [problem description]
- ⚠ [file] : [recommendation]
```
