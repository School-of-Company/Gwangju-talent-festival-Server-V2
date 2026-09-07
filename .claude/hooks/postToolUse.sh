#!/bin/bash
INPUT=$(cat)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULES_DIR="$SCRIPT_DIR/modules"

[[ -d "$MODULES_DIR" ]] || exit 0

for hook in "$MODULES_DIR"/*/postToolUse.sh; do
    [[ -f "$hook" ]] || continue
    printf "%s\n" "$INPUT" | bash "$hook"
done

exit 0