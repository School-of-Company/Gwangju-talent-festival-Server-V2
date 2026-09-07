#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Bash" ]] || [[ "$TOOL_NAME" == "shell" ]]; then
    COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // .tool_input.cmd // empty')
    BLOCKED_PATTERNS=(
        "rm -rf[[:space:]]*/[[:space:]]*$"
        "sudo rm"
        "> /dev/"
        "dd if="
        "mkfs"
        "curl.*\| sh"
        "wget.*\| sh"
    )
    for pattern in "${BLOCKED_PATTERNS[@]}"; do
        if [[ "$COMMAND" =~ $pattern ]]; then
            exit 2
        fi
    done
fi

exit 0