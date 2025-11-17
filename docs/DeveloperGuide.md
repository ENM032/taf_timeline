# TaF Timeline API — Developer Guide

## Architecture
- Javalin (HTTP server and routing)
- Jackson with `JavaTimeModule` for Java date/time serialization
- SQLite JDBC (Xerial) with HikariCP connection pool
- Domain: `Fact` model with `eventDate` and metadata

## Project Structure
- `com.timeline.api` — controllers and global handlers
- `com.timeline.api.error` — error DTOs and validation exception
- `com.timeline.config` — database bootstrap and lifecycle
- `com.timeline.model` — domain models
- `com.timeline.repository` — repository interfaces and SQLite implementation

## Build & Test
- Run tests: `mvn -q test`
- Package: `mvn -q -DskipTests package`
- Shaded JAR: `target\taf-timeline-0.1.0-shaded.jar`
- Test setup uses JavalinTest and in-memory SQLite URLs:
  - `jdbc:sqlite:file:memdb?mode=memory&cache=shared`

## Configuration
- Defaults:
  - Port: `8080`
  - DB URL: `jdbc:sqlite:data/timeline.db`
- Planned environment variable support (future work):
  - `PORT`, `DB_URL`, `CORS_ORIGINS`, rate limiting settings

## Error Handling Design
- Centralized in `GlobalExceptionHandler`
- Adds request correlation id; writes JSON error response with fields:
  - `timestamp`, `status`, `error`, `message`, `path`, `requestId`, `details`
- Exception mapping:
  - 400 (bad params/JSON), 404 (not found), 422 (validation), 503 (DB), 500 (unexpected)

## Repository & SQL
- `FactRepository` defines core operations:
  - `add`, `getById`, `getByMonth`, `getByDate`, `getRandom`, `update`, `delete`, `search`
- `SqliteFactRepository` implements queries and wraps `SQLException` in `DataAccessException`
- LIKE search escapes `%` and `_` with `ESCAPE '\'`
- Indices:
  - `idx_facts_event_date`, `idx_facts_category`, `idx_facts_created_at`

## Endpoint Semantics & Validation
- Query param parsing uses helpers for integers and bounds
- POST/PUT validation aggregates field errors (`title`, `summary`, `eventDate`) and returns 422
- `eventDate` must be `YYYY-MM-DD`
- `month` must be 1–12; `year` integer

## Testing Strategy
- API tests cover:
  - 400/404/422 responses, CRUD flow, search pagination
- Repository tests cover add/query by month

## Code Standards
- Naming: PascalCase classes, camelCase variables, UPPER_CASE constants
- Keep code self-explanatory; avoid comments unless logic is non-obvious
- Replace magic numbers/strings with constants where appropriate

## Data Migrations
- Plan: adopt Flyway to manage DDL (e.g., `V1__init.sql`)
- Move table/index creation from runtime to migrations (retain runtime checks as a safety net)

## Observability & Logging
- Plan: use Logback with JSON layout and SLF4J API
- Propagate `requestId` via MDC
- Access log: method, path, status, duration, requestId

## OpenAPI & Documentation
- Plan: integrate Javalin OpenAPI for schema generation
- Serve `/openapi` JSON and `/swagger` UI

## Docker Packaging
- Plan: Dockerfile using distroless Java base, env-driven config
- Health/readiness checks; DB file persisted via volume

## Security & Performance
- Validate all inputs and enforce bounds
- Safe search string escaping
- Consider rate limiting (token bucket per IP) and CORS allowlist
- Use least-privilege defaults and avoid leaking internals in error bodies

## Contributing & Workflow
- Branching: feature branches per change
- Git cycle: stage → commit → feature branch → push
- Commit messages: ≤72 chars, clear and descriptive
- Changelog: Semantic Versioning (MAJOR.MINOR.PATCH)
- Documentation: store in `/docs`; use ISO 8601 dates (YYYY-MM-DD HH:MM:SS, UTC)