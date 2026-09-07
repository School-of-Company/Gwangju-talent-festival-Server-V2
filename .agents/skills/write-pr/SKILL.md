---
name: write-pr
description: Generate PR title, body, and labels from commits since the base branch, then create the PR on GitHub. Handles base branch detection, label selection, and PR creation end-to-end.
allowed-tools: Bash(git *:*), Bash(bash *create-pr.sh:*), Bash(cat *:*), Read, Write
---

## Step 1 — Gather Context

```bash
git branch --show-current
git fetch origin develop --quiet 2>/dev/null || true
git log origin/develop..HEAD --oneline 2>/dev/null || git log --oneline -15
git diff origin/develop...HEAD --stat 2>/dev/null || git diff HEAD~5...HEAD --stat
git diff origin/develop...HEAD 2>/dev/null || git diff HEAD~5...HEAD
```

Also read the PR template:

```bash
cat .github/PULL_REQUEST_TEMPLATE.md
```

## Step 2 — Determine Labels

Read `.agents/skills/write-pr/references/labels.md` and select 1–2 appropriate labels based on the nature of the changes.
Read `.agents/skills/write-pr/references/commit-conventions.md` for commit type and scope naming rules.

## Step 3 — Generate PR Content

**Title** — Generate 3 options in the format `[scope] description`:

- Scope: determine from changed file paths and directory structure (`server` / `catalog`, or infer a more specific one from the path). Lowercase, wrapped in brackets: `[server]`, `[catalog]`. Use `[global]` / `[ci/cd]` for cross-cutting changes only.
- Description: Korean, concise, no emojis, max 50 characters total
- Wrap class names, method names, annotations, file names, and technical terms in backticks (e.g., `@Transactional`, `QueryProjectServiceImpl`, `SKILL.md`)

**Body** — Follow the `.github/PULL_REQUEST_TEMPLATE.md` structure:

- Korean 합쇼체: `~하였습니다`, `~되었습니다`, `~추가하였습니다`
- No emojis
- Max 2500 characters
- Wrap all proper nouns and technical identifiers in backticks: class names, method names, annotations, file names, field names, config keys, module names, and agent names.

## Step 4 — Write Body & Show Preview

Write the body to `PR_BODY.md`, then display:

```
## PR 제목 후보
1. [title1]
2. [title2]
3. [title3]

## 선택된 라벨
- label1, label2

## PR 본문 미리보기
[body content]
```

Ask the user which title to use (present options 1/2/3). Wait for the answer before proceeding.

## Step 5 — Create PR

Run the creation script with the confirmed title and labels:

```bash
bash .agents/skills/write-pr/scripts/create-pr.sh "<confirmed-title>" "PR_BODY.md" "<label1>,<label2>"
```

After creation, display the PR URL.
Cleanup: remove `PR_BODY.md`.
