---
name: api-design
description: REST API design guide for new endpoints — RESTful URL structure, DTO-only arguments for controllers and services, query parameter binding rules (@RequestParam vs @ModelAttribute), OpenAPI annotations, and response format.
---

# REST API Design Guide

## URL Design

- RESTful principles: `/v1/auth/api-keys`
- Use plural: `/students`, `/clubs`
- Hierarchy: `/students/{id}/projects`

## Everything Crosses a Boundary as a DTO

Controllers and services never take loose positional parameters. Every argument that crosses a boundary is
a DTO, and every response is a DTO:

```kotlin
// request body → ReqDto
@PostMapping("/api-keys")
fun create(@Valid @RequestBody reqDto: CreateApiKeyReqDto): ApiKeyResDto

// query parameters → @ModelAttribute ReqDto, not a pile of @RequestParam
@GetMapping("/students")
fun query(@Valid @ModelAttribute reqDto: QueryStudentReqDto): List<StudentResDto>

// service takes the same DTO — not (name, grade, status, page, size)
fun query(reqDto: QueryStudentReqDto): List<StudentResDto>
```

Why: adding a field doesn't ripple through every signature, argument order can't be mixed up, and
validation annotations live next to the shape they describe.

### `@RequestParam` vs `@ModelAttribute`

- **`@ModelAttribute` + ReqDto** — the default for query parameters. Also the only way to attach
  `@Valid` constraints to them.
- **`@RequestParam`** — only for a single, self-contained value that will never grow (e.g. `?force=true`).
  Two or more parameters means a DTO.
- Path variables (`@PathVariable`) stay as primitives — they're part of the URL, not a payload.

## Query Parameters

- Filtering: `?status=active`
- Pagination: `?page=0&size=20`
- Sorting: `?sort=createdAt,desc`

## OpenAPI Documentation

```kotlin
@Operation(summary = "Create API key", description = "...")
@ApiResponse(responseCode = "200", description = "Success")
@PostMapping("/api-keys")
fun create(@Valid @RequestBody reqDto: CreateApiKeyReqDto): ApiKeyResDto
```

## Response Format

- Success: return the `ResDto` directly — no envelope/wrapper type. The HTTP status carries the outcome.
- Error: throw a domain exception → global exception handler turns it into the error body.

Don't wrap successful payloads in a `data` field. Clients read the resource straight from the body, so an
envelope only adds a layer to unwrap on every call.
