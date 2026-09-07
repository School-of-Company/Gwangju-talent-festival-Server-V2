---
name: test
description: Run tests with coverage analysis and report results. Determines appropriate test scope (single test / module / all) based on context and analyzes failures in detail.
allowed-tools: Bash, Glob, Grep
---

## Determine Test Scope

Based on the user's request or changed files, choose the appropriate scope:

| Scope               | Command                                              |
|---------------------|------------------------------------------------------|
| Specific test class | `./gradlew test --tests "fully.qualified.ClassName"` |
| Specific module     | `./gradlew :<module>:test`                           |
| All modules         | `./gradlew test`                                     |

Discover available modules at runtime from the project structure (e.g., `settings.gradle.kts`, `package.json` workspaces, `pom.xml` modules).

## Run Tests

Execute the chosen command. Add `--info` if failures need detailed output:

```bash
./gradlew <scope> --info
```

## Analyze Results

After the run, report:

- Total tests / passed / failed / skipped
- Execution time
- For each failure:
  - Test name and class
  - Failure message and root cause
  - Relevant stack trace lines

If there are failures, read the relevant source files and suggest the most likely fix.
