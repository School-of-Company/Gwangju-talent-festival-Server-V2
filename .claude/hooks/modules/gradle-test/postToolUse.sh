#!/bin/bash
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name')

[[ "$TOOL_NAME" == "Edit" || "$TOOL_NAME" == "Write" ]] || exit 0

FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
CWD=$(echo "$INPUT" | jq -r '.cwd // empty')

[[ "$FILE_PATH" == *.kt || "$FILE_PATH" == *.java ]] || exit 0
[[ "$FILE_PATH" == */test/* ]] && exit 0
FILE_NAME=$(basename "$FILE_PATH")
[[ "$FILE_NAME" == *ServiceImpl.kt || "$FILE_NAME" == *ServiceImpl.java ]] || exit 0

if [[ "$FILE_PATH" == /* ]]; then
    FILE_ABS="$FILE_PATH"
elif [[ -n "$CWD" ]]; then
    FILE_ABS="$CWD/$FILE_PATH"
else
    exit 0
fi

PROJECT_ROOT=$(git -C "$(dirname "$FILE_ABS")" rev-parse --show-toplevel 2>/dev/null)
[[ -z "$PROJECT_ROOT" ]] && PROJECT_ROOT="$CWD"
[[ -z "$PROJECT_ROOT" ]] && exit 0

MODULE_DIR="${FILE_ABS%%/src/*}"
[[ "$MODULE_DIR" == "$FILE_ABS" ]] && exit 0
[[ -d "$MODULE_DIR/src/test" ]] || exit 0

if [[ -x "$PROJECT_ROOT/gradlew" ]]; then
    GRADLE_CMD="$PROJECT_ROOT/gradlew"
elif command -v gradle >/dev/null 2>&1; then
    GRADLE_CMD="gradle"
else
    echo "[Hook] gradlew/gradle 를 찾지 못해 테스트를 건너뜁니다." >&2
    exit 0
fi

REL="${MODULE_DIR#$PROJECT_ROOT}"
REL="${REL#/}"
if [[ -z "$REL" ]]; then
    TEST_TASK=":test"
else
    TEST_TASK=":${REL//\//:}:test"
fi

BASE="${FILE_NAME%.*}"
TEST_CLASS="${BASE%Impl}Test"
echo "[Hook] Running $TEST_TASK --tests $TEST_CLASS ..." >&2
TEST_OUTPUT=$(cd "$PROJECT_ROOT" && "$GRADLE_CMD" "$TEST_TASK" --tests "$TEST_CLASS" 2>&1)
TEST_EXIT=$?
TAIL=$(echo "$TEST_OUTPUT" | tail -5)
if [[ $TEST_EXIT -ne 0 ]]; then
    echo "[Hook] Test FAILED ($TEST_TASK). Last 5 lines:"
    echo "$TAIL"
    echo "Tests failed after editing $FILE_NAME. Consider running test-fixer agent."
else
    echo "[Hook] Tests passed ($TEST_TASK). Last 5 lines:"
    echo "$TAIL"
fi
exit 0