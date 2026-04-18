#!/bin/bash
# .claude/hooks/preCommit.sh
# Run compileJava before commit if any staged file is a Java file

# Only run for git commit commands
if [[ "$TOOL_PARAMS_COMMAND" != *"git commit"* ]]; then
    exit 0
fi

STAGED_JAVA=$(git diff --cached --name-only | grep "\.java$")

if [[ -n "$STAGED_JAVA" ]]; then
    echo "[Hook] Java file detected in staged changes — running compileJava"
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
    cd "$PROJECT_ROOT"
    ./gradlew compileJava -q
    if [ $? -eq 0 ]; then
        echo "[Hook] ✓ Compile successful"
    else
        echo "[Hook] ✗ Compile failed — commit aborted"
        exit 1
    fi
fi