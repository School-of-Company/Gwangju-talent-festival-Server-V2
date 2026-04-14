---
name: commit
description: Create Git commits by splitting changes into logical units following project conventions. Handles Git Flow automatically — detects develop branch and checks out a feature branch before committing.
allowed-tools: Bash
---

## Step 0 — Branch Check (Required)

Check the current branch first:

```bash
git branch --show-current
```

**If current branch is `develop`:**

This project uses Git Flow. Feature branches must be created from `develop` and merged back into `develop`.

1. Analyze all changes with `git status` and `git diff`
2. Infer an appropriate branch name from the changes:
   - Format: `<type>/<kebab-case-description>` — use the same type as the planned commit
   - Reflect the domain scope in the name
   - Examples: `add/add-team-list-api`, `fix/auth-login-bug`, `update/optimize-seat-query`
3. Create and checkout the branch:
   ```bash
   git checkout -b <type>/<inferred-name>
   ```
4. Proceed with the commit flow below

**If current branch is NOT `develop`:** proceed directly to the commit flow.

---

## Commit Message Rules

Format: `type :: description`

- **Types**: `add` / `update` / `fix` / `delete` / `docs` / `test` / `merge` / `init`
- **Description**: Korean, no period, use noun-ending style; forbidden endings: `~한다/~된다`, `~하기`, `~합니다/~됩니다`, `~했습니다`
   - Good: `엔티티 필드 추가`, `트랜잭션 롤백 방지`, `로직 개선`
- Subject line only (no body)
- Do NOT add AI as co-author

## Commit Flow

1. Inspect changes: `git status`, `git diff`
2. Group changed files by logical unit of change:
   - Same feature or bug fix → one commit
   - Related files that must change together (e.g. entity + service + controller for one feature) → one commit
   - Unrelated changes → separate commits
3. For each logical group:
   - Stage the relevant files: `git add <file1> <file2> ...`
   - Write a commit message that describes the change as a whole
   - `git commit -m "message"`
4. Verify with `git log --oneline -n <count>`

> **Rule**: One logical change = One commit. Files that must change together belong in the same commit. Unrelated changes must be split.