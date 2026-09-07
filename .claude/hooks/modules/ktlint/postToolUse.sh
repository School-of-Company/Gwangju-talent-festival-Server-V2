#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')
    if [[ "$FILE_PATH" == *.kt ]]; then
        echo "[Hook] Running ktlintFormat for $(basename "$FILE_PATH")" >&2
        cd "$CWD"
        if ./gradlew ktlintFormat -q 2>&1; then
            echo "[Hook] Format OK" >&2
        else
            echo "[Hook] Format failed" >&2
        fi
    fi
fi
exit 0
