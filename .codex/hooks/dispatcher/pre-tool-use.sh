#!/bin/bash
INPUT=$(cat)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULES_DIR="$SCRIPT_DIR/modules"
[[ -d "$MODULES_DIR" ]] || MODULES_DIR="${SCRIPT_DIR%/*}/modules"

[[ -d "$MODULES_DIR" ]] || exit 0

for hook in "$MODULES_DIR"/*/pre-tool-use.sh; do
    [[ -f "$hook" ]] || continue
    echo "$INPUT" | bash "$hook"
    if [[ $? -eq 2 ]]; then
        echo '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"deny","permissionDecisionReason":"Dangerous command blocked by project hook"}}'
        exit 0
    fi
done

exit 0
