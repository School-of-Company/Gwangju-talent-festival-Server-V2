#!/bin/bash
set -e

PR_NUMBER=$(gh pr view --json number -q .number 2>/dev/null)
if [ -z "$PR_NUMBER" ]; then
  echo "ERROR: No open PR found for current branch." >&2
  exit 1
fi

REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
BASE=$(gh pr view "$PR_NUMBER" --json baseRefName -q .baseRefName)

mkdir -p .pr-tmp

gh api "repos/$REPO/pulls/$PR_NUMBER/comments" \
  --jq '[.[] | {id, path, line, body, user: .user.login}]' \
  > .pr-tmp/pr_comments.json

git log "origin/$BASE..HEAD" --pretty=format:"%H %h %s" > .pr-tmp/pr_commits.txt

git diff "origin/$BASE...HEAD" --name-only > .pr-tmp/pr_changed_files.txt

git diff "origin/$BASE...HEAD" > .pr-tmp/pr_diff.txt

echo "PR #$PR_NUMBER | Repo: $REPO | Base: $BASE"
echo "Comments: $(jq length .pr-tmp/pr_comments.json), Changed files: $(wc -l < .pr-tmp/pr_changed_files.txt | tr -d ' ')"
