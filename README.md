# TaF Timeline API

## Overview
TaF Timeline is a lightweight REST API that serves curated facts for specific months and days, backed by SQLite. It’s suitable for timelines of events (current and historical), month-by-month browsing, and simple curation workflows.

## Features
- Facts by month and specific day
- Create, update, delete facts
- Random fact for a given month
- Search with pagination and sorting
- Consistent JSON error responses with request correlation IDs

## Quick Start
- Run tests: `mvn -q test`
- Build package: `mvn -q -DskipTests package`
- Start server: `java -jar target\taf-timeline-0.1.0.jar`
- Default port: `8080`
- Default DB: `jdbc:sqlite:var/db/timeline.db` (auto-created)

## API Summary
- `GET /health` — health check
- `GET /api/facts?year=YYYY&month=M` — facts for month
- `GET /api/facts/{id}` — fact by id
- `POST /api/facts` — create
- `PUT /api/facts/{id}` — update
- `DELETE /api/facts/{id}` — delete
- `GET /api/facts/on?date=YYYY-MM-DD` — facts on a day
- `GET /api/facts/random?year=&month=` — random fact in month
- `GET /api/facts/search?year=&month=&category=&q=&page=&size=&sort=` — search + pagination

## Error Handling
Errors return JSON with: `timestamp`, `status`, `error`, `message`, `path`, `requestId`, `details`. Common statuses: 400, 404, 422, 503, 500.

## Configuration
- Port: `8080`
- DB URL: `jdbc:sqlite:var/db/timeline.db`

## Documentation
- Detailed docs are in `docs/`:
  - Developers: `docs/DeveloperGuide.md`
  - Users: `docs/UserGuide.md`

## Disclaimer
This project uses AI-generated documentation, commit messages, and pull requests.