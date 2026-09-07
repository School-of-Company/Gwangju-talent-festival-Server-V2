#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd')
    case "$FILE_PATH" in
        *.java|*.kt|*.groovy)
            echo "[Hook] Running spotlessApply for $(basename "$FILE_PATH")" >&2
            cd "$CWD"
            if ./gradlew spotlessApply -q 2>&1; then
                echo "[Hook] spotless OK" >&2
            else
                echo "[Hook] spotless failed" >&2
            fi
            ;;
    esac
fi
exit 0
