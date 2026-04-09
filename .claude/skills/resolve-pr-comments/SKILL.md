---
name: resolve-pr-comments
description: For each inline PR review comment, judge whether it has been resolved in the current branch diff and reply with the resolving commit hash. Use after addressing PR feedback.
allowed-tools: Bash(bash *get-pr-data.sh:*), Bash(gh api:*), Bash(gh pr view:*), Bash(gh repo:*), Bash(git log:*), Bash(git push:*), Bash(rm:*), Read
---

## Step 1 — Collect PR Data

```bash
bash "${CLAUDE_SKILL_DIR}/scripts/get-pr-data.sh"
```

Then read the output files:
- `.pr-tmp/pr_comments.json` — inline review comments
- `.pr-tmp/pr_changed_files.txt` — changed files
- `.pr-tmp/pr_commits.txt` — commits in this PR
- `.pr-tmp/pr_diff.txt` — full diff

Also fetch repo and PR metadata:

```bash
gh repo view --json nameWithOwner -q .nameWithOwner
gh pr view --json number -q .number
```

## Step 2 — Judge Each Comment

For each comment in `pr_comments.json`, compare `path` + `body` against the diff in `pr_diff.txt`:

- **RESOLVED**: the diff contains a change in the same file that directly addresses the comment's concern
- **UNRESOLVED**: no relevant change found for that file/concern

## Step 3 — Find Commit Hash (resolved only)

```bash
git log "origin/$(gh pr view --json baseRefName -q .baseRefName)..HEAD" --follow --pretty="%H %h %s" -- "<path>"
```

Select the 7-character short hash of the most relevant commit.

## Step 4 — Push & Post Reply (resolved only)

First push any unpushed commits, then post the reply.

```bash
git push
```

Always quote variables to prevent shell injection. `path` and `comment_id` come from external PR data and may contain special characters.

```bash
gh api "repos/<owner>/<repo>/pulls/<pr_number>/comments/<comment_id>/replies" \
  -f body="<short_hash> 에서 해결했습니다."
```

## Step 5 — Report

```
## 답변 완료 코멘트
- [file:line] "comment excerpt" → {hash} 해결했습니다.
...

## 미해결 코멘트 (reply 없음)
- [file:line] "comment excerpt" → 사유: ...
...
```

## Step 6 — Cleanup

```bash
rm -rf .pr-tmp
```
