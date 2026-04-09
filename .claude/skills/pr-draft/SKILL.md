---
name: pr-draft
description: Generate PR title, body, and labels from commits since the base branch, then create the PR on GitHub.
allowed-tools: Bash(git *:*), Bash(bash *create-pr.sh:*), Bash(cat *:*), Read, Write
---

## Step 1 — Gather Context

```bash
git branch --show-current
git log origin/develop..HEAD --oneline 2>/dev/null || git log --oneline -15
git diff origin/develop...HEAD --stat 2>/dev/null || git diff HEAD~5...HEAD --stat
git diff origin/develop...HEAD 2>/dev/null || git diff HEAD~5...HEAD
```

Also read the PR template:

```bash
cat .github/PULL_REQUEST_TEMPLATE.md
```

## Step 2 — Determine Labels

Read `${CLAUDE_SKILL_DIR}/references/labels.md` and select 1–2 appropriate labels.

## Step 3 — Generate PR Content

**Title** — Generate 3 options:
- Format: `[type] description`
- Description: Korean, concise, no period, max 50 characters total
- Mark the best option with `← 추천`

**Body** — Follow `.github/PULL_REQUEST_TEMPLATE.md` structure:
- Write all content in Korean
- Style: `~하였습니다`, `~되었습니다`, `~추가하였습니다`
- No additional emojis, max 2500 characters
- Auto-check checklist items based on the nature of changes
- Infer related issue number from branch name or commits if possible

## Step 4 — Write Body & Show Preview

Write the body to `PR_BODY.md`, then display:

```
## 추천 PR 제목
1. [title1]
2. [title2]
3. [title3] ← 추천

## 선택된 라벨
- label1, label2

## PR 본문 미리보기
[body content]
```

Ask the user to confirm which title to use. If no answer, proceed with the recommended title.

## Step 5 — Create PR

```bash
bash "${CLAUDE_SKILL_DIR}/scripts/create-pr.sh" "<confirmed-title>" "PR_BODY.md" "<label1>,<label2>"
```

After creation, display the PR URL.
Cleanup: remove `PR_BODY.md`.