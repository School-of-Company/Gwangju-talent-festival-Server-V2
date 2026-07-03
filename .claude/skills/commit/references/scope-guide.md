# Scope Selection Guide

## Domain Names

| Scope | Description |
|-------|-------------|
| auth | Authentication / Authorization |
| judge | Judging |
| seat | Seat reservation |
| slogan | Slogan submission |
| team | Performance team management |
| user | Member |

## Module Names (Cross-cutting concerns only)

| Scope | Description |
|-------|-------------|
| global | Affects multiple modules |
| ci/cd | Build / deployment |

## Examples

**Wrong:**
- `fix/login-bug` → `fix/auth-login-bug`

**Correct:**
- `add/team-list-api`
- `fix/auth-login-bug`
- `update/seat-reservation-logic`