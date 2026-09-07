#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]] || [[ "$TOOL_NAME" == "write_file" ]]; then
    FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')
    CWD=$(echo "$INPUT" | jq -r '.cwd // empty')

    case "$FILE_PATH" in
        *.java|*.kt|*.groovy)
            [[ -z "$CWD" ]] && exit 0
            PROJECT_ROOT=$(git -C "$CWD" rev-parse --show-toplevel 2>/dev/null || printf '%s' "$CWD")
            echo "[Hook] Running spotlessApply for $(basename "$FILE_PATH")" >&2
            cd "$PROJECT_ROOT"
            if ./gradlew spotlessApply -q 2>&1; then
                echo "[Hook] spotless OK" >&2
            else
                echo "[Hook] spotless failed" >&2
            fi
            ;;
    esac
fi

exit 0
