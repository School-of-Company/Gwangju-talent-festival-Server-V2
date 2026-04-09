#!/bin/bash
set -e

TITLE="${1:?Error: PR title is required. Usage: create-pr.sh <title> <body-file> [label1,label2,...]}"
BODY_FILE="${2:?Error: Body file is required. Usage: create-pr.sh <title> <body-file> [label1,label2,...]}"
LABELS="${3:-}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
TEMPLATE="$PROJECT_ROOT/.github/PULL_REQUEST_TEMPLATE.md"

# If body file doesn't exist, fall back to PR template
if [ ! -f "$BODY_FILE" ]; then
  if [ -f "$TEMPLATE" ]; then
    echo "Body file not found — using PR template: $TEMPLATE"
    BODY_FILE="$TEMPLATE"
  else
    echo "ERROR: Body file not found: $BODY_FILE" >&2
    exit 1
  fi
fi

# If body file is empty, fall back to PR template
if [ ! -s "$BODY_FILE" ] && [ -f "$TEMPLATE" ]; then
  echo "Body file is empty — using PR template: $TEMPLATE"
  BODY_FILE="$TEMPLATE"
fi

CURRENT=$(git branch --show-current)
case "$CURRENT" in
  feature/*)  BASE="develop" ;;
  fix/*)      BASE="develop" ;;
  hotfix/*)   BASE="main" ;;
  develop)    BASE="main" ;;
  *)          BASE=$(gh pr view --json baseRefName -q .baseRefName 2>/dev/null || echo "develop") ;;
esac

ARGS=(gh pr create --title "$TITLE" --body-file "$BODY_FILE" --base "$BASE")

if [ -n "$LABELS" ]; then
  IFS=',' read -ra LABEL_ARRAY <<< "$LABELS"
  for label in "${LABEL_ARRAY[@]}"; do
    trimmed=$(echo "$label" | xargs)
    [ -n "$trimmed" ] && ARGS+=(--label "$trimmed")
  done
fi

echo "Creating PR..."
echo "  Title : $TITLE"
echo "  Base  : $BASE"
[ -n "$LABELS" ] && echo "  Labels: $LABELS"
echo ""

"${ARGS[@]}"