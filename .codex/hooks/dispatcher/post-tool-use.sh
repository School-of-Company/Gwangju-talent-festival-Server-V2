#!/bin/bash
INPUT=$(cat)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULES_DIR="$SCRIPT_DIR/modules"
[[ -d "$MODULES_DIR" ]] || MODULES_DIR="${SCRIPT_DIR%/*}/modules"

[[ -d "$MODULES_DIR" ]] || exit 0

for hook in "$MODULES_DIR"/*/post-tool-use.sh; do
    [[ -f "$hook" ]] || continue
    echo "$INPUT" | bash "$hook"
done

exit 0
