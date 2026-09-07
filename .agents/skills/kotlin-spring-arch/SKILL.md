---
name: kotlin-spring-arch
description: Architecture reference for this project — Controller/Service/Repository layer responsibilities, @Transactional strategy (readOnly optimization, N+1 prevention), ExpectedException usage (no subclasses), and Entity↔DTO conversion patterns.
---

# Kotlin + Spring Boot Architecture Guide

## Layer Structure

### Controller
- Role: Request validation, DTO conversion, HTTP response
- Annotations: `@RestController`, `@RequestMapping`
- Validation: `@Valid`, `@Validated`
- Response: return the ResDto directly — no envelope wrapper (see the `api-design` skill)

### Service
- Role: Business logic, transaction management
- Pattern: interface + implementation
- Transaction:
  - Read: `@Transactional(readOnly = true)`
  - Write: `@Transactional`
- Dependencies: Inject Repository via constructor injection

### Repository
- Role: Data access
- JPA: Extend `JpaRepository`
- QueryDSL: Complex queries
- Avoid N+1: Fetch Join, `@EntityGraph`

## Transaction Strategy

### Read-only Optimization
```kotlin
@Transactional(readOnly = true)
fun findApiKeys(): List<ApiKeyResDto> {
    return repository.findAll()
        .map { it.toResDto() }
}
```

### N+1 Problem Resolution
```kotlin
// ❌ N+1 occurs
repository.findAll() // 1 query
entity.relatedEntity // N queries

// ✅ Fetch Join
@Query("SELECT e FROM Entity e JOIN FETCH e.relatedEntity")
fun findAllWithRelated(): List<Entity>
```

## Exception Handling

### Use ExpectedException Directly
```kotlin
val apiKey = repository.findById(id).orElseThrow {
    ExpectedException("API key를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
}
```

Do not create custom exception subclasses extending `ExpectedException`.

### Global Handler
Locate with: `find . -name "GlobalExceptionHandler.kt" ! -path "*/build/*"`

## DTO Conversion Pattern

```kotlin
// Entity → ResDto
fun Entity.toResDto() = EntityResDto(
    id = this.id,
    name = this.name
)

// ReqDto → Entity
fun EntityReqDto.toEntity() = Entity(
    name = this.name
)
```
