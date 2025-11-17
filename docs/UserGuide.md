# TaF Timeline API — User Guide

## Overview
TaF Timeline is a lightweight REST API that serves curated facts for specific months and days, with SQLite storage. Typical uses include browsing monthly timelines, querying notable historical events, and adding curated facts.

## System Requirements
- Windows 10 Pro (or any OS with Java 17+)
- Java 17+ Runtime (Temurin recommended)
- Maven 3.9+ or Gradle 8+
- Network access to `localhost` for testing

## Installation
Using Chocolatey (recommended on Windows):
- `choco install temurin`
- `choco install maven`
- `choco install gradle`

Verify installations:
- `java -version`
- `mvn -v`
- `gradle -v`

## Running the Server
From the project directory:
- Run tests: `mvn -q test`
- Build package: `mvn -q -DskipTests package`
- Start server: `java -jar target\taf-timeline-0.1.0.jar`

Default settings:
- Server listens at `http://localhost:8080/`
- SQLite database file at `data\timeline.db` (auto-created)
- On first run, sample January data is seeded if DB is empty

## API Reference
All responses are JSON and use ISO 8601 date formats.

- Health
  - `GET /health`
  - Returns `{ "status": "ok" }`

- Facts by Month
  - `GET /api/facts?year=YYYY&month=M`
  - Params: `year` integer, `month` 1–12

- Fact by ID
  - `GET /api/facts/{id}`

- Create Fact
  - `POST /api/facts`
  - Body: `{ "eventDate": "YYYY-MM-DD", "title": "...", "summary": "...", "category": "...", "sourceUrl": "..." }`

- Update Fact
  - `PUT /api/facts/{id}`
  - Same body schema as POST

- Delete Fact
  - `DELETE /api/facts/{id}` (204 on success)

- Facts on Specific Day
  - `GET /api/facts/on?date=YYYY-MM-DD`

- Random Fact in Month
  - `GET /api/facts/random?year=YYYY&month=M`

- Search with Pagination
  - `GET /api/facts/search?year=&month=&category=&q=&page=&size=&sort=`
  - `page` ≥ 0, `size` 1–100, `sort` like `event_date,asc` or `title,desc`
  - `q` matches `title` or `summary`; special characters are safely escaped

## Error Handling
Errors are returned as JSON:
```
{
  "timestamp": "2025-11-15T20:12:34Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Validation failed",
  "path": "/api/facts",
  "requestId": "c0f7b9e1-...",
  "details": { "eventDate": "must be YYYY-MM-DD", "title": "required" }
}
```
- 400 Bad Request: malformed params or JSON
- 404 Not Found: missing resource
- 422 Unprocessable Entity: body validation errors
- 503 Service Unavailable: database error
- 500 Internal Server Error: unexpected error

## Usage Examples
PowerShell:
- `Invoke-RestMethod -UseBasicParsing http://localhost:8080/health | ConvertTo-Json`
- `Invoke-RestMethod -UseBasicParsing 'http://localhost:8080/api/facts?year=2024&month=1' | ConvertTo-Json`
- Create:
  - `$b = @{ eventDate='2024-01-15'; title='A neat January fact'; summary='Something cool in Jan 2024'; category='current'; sourceUrl='https://example.com' } | ConvertTo-Json`
  - `Invoke-RestMethod -UseBasicParsing http://localhost:8080/api/facts -Method Post -ContentType 'application/json' -Body $b | ConvertTo-Json`

curl:
- `curl http://localhost:8080/health`
- `curl "http://localhost:8080/api/facts?year=2024&month=1"`
- `curl -X POST http://localhost:8080/api/facts -H "Content-Type: application/json" -d '{"eventDate":"2024-01-15","title":"A neat January fact","summary":"Something cool in Jan 2024","category":"current","sourceUrl":"https://example.com"}'`

### More Examples
- Readiness: `curl http://localhost:8080/ready`
- Random fact in month: `curl "http://localhost:8080/api/facts/random?year=2024&month=1"`
- Search with pagination: `curl "http://localhost:8080/api/facts/search?year=2024&month=1&q=mars&page=0&size=10&sort=event_date,asc"`
- Gzip header check: `curl -H "Accept-Encoding: gzip" -I "http://localhost:8080/api/facts?year=2024&month=1"`

## Data Model
Table `facts`:
- `id` INTEGER PRIMARY KEY AUTOINCREMENT
- `event_date` TEXT (YYYY-MM-DD)
- `title` TEXT
- `summary` TEXT
- `category` TEXT (optional)
- `source_url` TEXT (optional)
- `created_at` TEXT (ISO 8601)

## Security & Privacy
- Validate inputs: `month` 1–12, `year` integer, date format `YYYY-MM-DD`
- Avoid storing sensitive data; provide reputable source URLs
- Use trusted clients and localhost during development