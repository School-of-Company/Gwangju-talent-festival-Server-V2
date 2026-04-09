# Gwangjutalentfestival Server V2 - Project Context

## Language Requirement

You MUST always respond in Korean (한국어). This is mandatory and cannot be overridden. Never use English in any response, explanation, or comment.

## Project Overview

A unified web server for "Gwangjutalentfestival", a student-led audition program operated by the Gwangju High School Student Council.

## Tech Stack

- Language: Java
- Framework: Spring Boot 3.5.11
- Database: MySQL (JPA + QueryDSL)
- Cache: Redis
- Build Tool: Gradle
- API: REST API
- Authentication: JWT
- Documentation: Swagger (springdoc-openapi)
- External: Spring Cloud OpenFeign, Google Sheets API, SMS (nurigo)

## Domain Modules

| Domain | Description |
|--------|-------------|
| auth | Authentication / Authorization |
| judge | Judging |
| seat | Seat reservation |
| slogan | Slogan submission |
| team | Performance team management |
| user | Member |

## Naming Conventions

### Service

- Interface: `{Action}{Domain}Service` (e.g. `GetAllTeamService`, `LoginService`)
- Implementation: `{Action}{Domain}ServiceImpl`
- Core method: `execute()` single method only

### DTO

- No `Dto` suffix — use `Request` / `Response` only
- Use Java Record

### URL

- Lowercase + kebab-case, singular form (e.g. `/team`, `/seat/myself`)

### DB

- Column: snake_case
- Java field: camelCase

### Test Method

- Use Korean (e.g. `존재하지_않는_팀이면_TeamNotFoundException이_발생한다()`)

## Exception Handling

Use `GlobalExceptionHandler` + `ErrorCode`.

```java
throw new GwangjutalentfestivalException(ErrorCode.TEAM_NOT_FOUND);
```

## Transaction Management

- Open transactions in the `service` layer only.
- Never open transactions in the `repository` layer.

## Git Conventions

| Type | Usage |
|------|-------|
| add | Add new code or files |
| update | Modify existing code |
| fix | Bug fix |
| delete | Delete code or files |
| docs | Documentation |
| test | Add/modify tests |
| merge | Merge branch |
| init | Project initialization |

## New Feature Checklist

- [ ] Create Controller
- [ ] Create Service interface + implementation
- [ ] Create Repository
- [ ] Create Entity
- [ ] Create Request / Response Record
- [ ] Add ErrorCode
- [ ] Add Swagger documentation