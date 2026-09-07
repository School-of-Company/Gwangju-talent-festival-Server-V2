#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
if [[ "$TOOL_NAME" == "Bash" ]]; then
    COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command')
    BLOCKED_PATTERNS=(
        "rm -rf[[:space:]]*/[[:space:]]*$"
        "sudo rm"
        "> /dev/(sd[a-z]|hd[a-z]|nvme|vg|mem|kmem|port)"
        "dd if="
        "mkfs"
        "curl.*\| sh"
        "wget.*\| sh"
    )
    for pattern in "${BLOCKED_PATTERNS[@]}"; do
        if [[ "$COMMAND" =~ $pattern ]]; then
            echo "[Hook] Blocked dangerous command: $COMMAND" >&2
            exit 2
        fi
    done
fi
exit 0
