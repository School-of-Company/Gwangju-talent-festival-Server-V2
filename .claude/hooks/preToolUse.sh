#!/bin/bash
# .claude/hooks/preToolUse.sh
# Block dangerous commands before execution

if [[ "$TOOL_NAME" == "Bash" ]]; then
    COMMAND="$TOOL_PARAMS_COMMAND"
    BLOCKED_PATTERNS=(
        "rm -rf /"
        "sudo rm -rf"
        "> /dev/"
        "dd if="
        "mkfs"
        "curl.*\| sh"
        "wget.*\| sh"
    )
    for pattern in "${BLOCKED_PATTERNS[@]}"; do
        if [[ "$COMMAND" =~ $pattern ]]; then
            echo "[Hook] ✗ Blocked dangerous command: $COMMAND"
            echo "This command is not allowed for safety reasons."
            exit 2
        fi
    done
fi