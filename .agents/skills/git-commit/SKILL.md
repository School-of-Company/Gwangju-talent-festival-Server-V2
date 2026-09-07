---
name: git-commit
description: Create Git commits following this project's Conventional Commits style. Splits changes into logical units and writes concise Korean-description commit messages.
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
   - Reflect the scope in the name
   - Examples: `feat/repo-select-dropdown`, `fix/base-branch-check`
3. Create and checkout the branch:
   ```bash
   git checkout -b <type>/<inferred-name>
   ```
4. Proceed with the commit flow below

**If current branch is NOT `develop`:** proceed directly to the commit flow.

---

## Commit Message Rules

Format: `type(scope): description`

- **Type**: `feat` / `fix` / `refactor` / `docs` / `chore` / `test`
- **Scope**: `web` / `server` / `catalog` (레포 영역 기준, 필요하면 더 세분화 가능)
- **Description**: 한글, 명사형 종결, 마침표 없음
  - Good: `레포 선택 드롭다운 구현`, `PR 생성 시 base branch 조회 실패 처리`
- Subject line only (no body) — breaking change일 때만 예외적으로 본문에 `BREAKING CHANGE: <설명>` 추가
- Do NOT add AI as co-author

## Commit Flow

1. Inspect changes: `git status`, `git diff`
2. Group changed files by logical unit of change:
   - Same feature or bug fix → one commit
   - Related files that must change together → one commit
   - Unrelated changes → separate commits
3. For each logical group:
   - Stage the relevant files: `git add <file1> <file2> ...`
   - Write a commit message: `type(scope): description`
   - `git commit -m "message"`
4. Verify with `git log --oneline -n <count>`

> **Rule**: One logical change = One commit. Files that must change together belong in the same commit. Unrelated changes must be split.
