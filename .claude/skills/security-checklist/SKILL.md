---
name: security-checklist
description: Verify security vulnerabilities — hardcoded secrets, injection, token validation, credential masking, sensitive logging, and authorization checks. Stack-agnostic. Run before merging any auth or API-related changes.
---

# Security Checklist

## Step 0 — Decide What to Search (do this first)

Every grep below needs the extensions this project actually uses. **Never assume a language.** A scan
pointed at the wrong extension returns zero hits, and zero hits reads exactly like "no problems found".

```bash
git ls-files | sed -n 's/.*\.\([A-Za-z0-9]*\)$/\1/p' | sort | uniq -c | sort -rn | head -12
```

Pick the source extensions from that output and reuse them in every command below:

```bash
SRC=(--include="*.kt" --include="*.java")                    # JVM
SRC=(--include="*.ts" --include="*.tsx" --include="*.js")    # TypeScript/JavaScript
SRC=(--include="*.py")                                       # Python
EXCL=(--exclude-dir=build --exclude-dir=target --exclude-dir=dist \
      --exclude-dir=.next --exclude-dir=node_modules --exclude-dir=.venv)
```

If a scan comes back empty, prove the extensions matched real files before reporting "clean":

```bash
grep -rl "" "${SRC[@]}" "${EXCL[@]}" . | head -3   # empty means SRC is wrong, not that the code is safe
```

## Verification Items

### 1. Hardcoded Secrets

- [ ] No API key, secret, or password literal in source?
- [ ] Loaded from environment variables or a secret store instead?

```bash
grep -rniE "(password|secret|api_?key|token|credential)s?[[:space:]]*[:=][[:space:]]*['\"][^'\"]{6,}" "${SRC[@]}" "${EXCL[@]}" .
grep -rniE "(password|secret|api_?key|token)" \
  --include="*.yml" --include="*.yaml" --include="*.properties" --include="*.toml" --include="*.json" "${EXCL[@]}" .
# high-entropy literals; expect false positives from hashes and test fixtures
grep -rE "['\"][A-Za-z0-9+/]{40,}={0,2}['\"]" "${SRC[@]}" "${EXCL[@]}" .
# provider-specific shapes worth looking at directly
grep -rE "(AKIA[0-9A-Z]{16}|gh[pousr]_[A-Za-z0-9]{20,}|sk-[A-Za-z0-9]{20,}|-----BEGIN [A-Z ]*PRIVATE KEY)" "${EXCL[@]}" .
```

Secret-shaped files must be _ignored_, not merely absent from the working tree:

```bash
git check-ignore -v .env *.pem 2>/dev/null || echo "not ignored — add to .gitignore"
git log --diff-filter=A --name-only --pretty=format: | grep -iE "\.(pem|key|p12|jks)$|(^|/)\.env" | sort -u
```

That second command earns its place: a credential committed once stays in history after the file is
deleted, so a hit there means **rotate the credential** — removing the file is not a fix.

**Limitations** — this is a first pass, not proof:

- Misses secrets assembled at runtime, or encoded beyond the patterns above
- Misses anything outside the repo (CI settings, deploy configs)
- Auth and payment paths still need to be read by a person

### 2. Injection

- [ ] Queries use parameter binding (prepared statement, ORM, query builder) — never string concatenation?
- [ ] Shell commands never interpolate user input?

```bash
grep -rniE "(select|insert|update|delete)[^;]*(\+|\\\$\{|%s|f\")" "${SRC[@]}" "${EXCL[@]}" .
grep -rniE "(exec|execSync|system\(|Runtime\.getRuntime|subprocess|child_process)" "${SRC[@]}" "${EXCL[@]}" .
```

### 3. Token / Session Validation

- [ ] Signature verified with an expected algorithm (reject `alg: none` and algorithm confusion)?
- [ ] Expiry checked?
- [ ] Issuer/audience claims validated, not just read?

```bash
grep -rniE "(decode|verify)[^(]*\(.*(jwt|token)" "${SRC[@]}" "${EXCL[@]}" .
```

Decoding is not validating — a `decode` with no `verify` beside it is exactly what this check is for.

### 4. Credential Handling

- [ ] Keys and tokens masked or omitted in API responses?
- [ ] Stored hashed or encrypted, never in plaintext?

### 5. Logging

- [ ] No passwords, tokens, or personal data in log lines?
- [ ] Log level appropriate (no request/response dumps at info)?

```bash
grep -rniE "log(ger)?\.(debug|info|warn|error).*(password|token|secret|authorization)" "${SRC[@]}" "${EXCL[@]}" .
```

### 6. Authorization

- [ ] Every endpoint needing auth actually enforces it — with whatever this project uses (framework
      annotation, filter, middleware, guard, decorator)?
- [ ] Ownership checked, so changing an id in the path can't reach another user's resource?

Find how this project declares protection, then look for the endpoints that lack it:

```bash
grep -rnoE "@(PreAuthorize|Secured|RolesAllowed|UseGuards|Roles)|requireAuth|isAuthenticated" "${SRC[@]}" "${EXCL[@]}" . \
  | sed 's/.*://' | sort | uniq -c | sort -rn
```

The counts matter less than the gap: list the route handlers, list the protected ones, and read the
difference. A public-by-default endpoint that takes a user id from the path is the classic finding.

## Reporting

State the scope alongside the findings — which extensions were searched and which directories skipped.
A checklist reported without its scope is indistinguishable from one that searched the wrong files.
