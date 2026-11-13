# PostWave

PostWave is a small full-stack social publishing application rebuilt for Java interview preparation. The backend is a production-style Spring Boot service with PostgreSQL persistence, JWT authentication, image uploads, integration tests, Docker packaging, and CI.

## Backend stack

- Java 21 and Spring Boot 4
- Spring MVC, Spring Security, JWT, Bean Validation
- Spring Data JPA, PostgreSQL 17, Flyway
- JUnit 5, MockMvc, Mockito, Testcontainers
- Docker Compose and GitHub Actions

## Features

- Register and log in with BCrypt password hashing
- Stateless Bearer-token authentication
- Public post list/detail, including popular sorting
- Authenticated create and wave/unwave
- Owner/admin authorization for edit and delete
- JPEG, PNG, GIF, and WebP uploads (maximum 5 MB)
- Stable JSON error codes and field validation errors
- Database migrations and real PostgreSQL integration tests

## Run locally

Requirements: Java 21 and Docker Desktop.

```powershell
docker compose up -d postgres
cd backend
.\mvnw.cmd spring-boot:run
```

The API runs at `http://localhost:8080`. Health check: `GET /api/health`. The OpenAPI contract is available at `GET /openapi.yaml`.

Run tests (Docker must be running because repository and API tests use Testcontainers):

```powershell
cd backend
.\mvnw.cmd test
```

## Run the containerized stack

Copy `.env.example` to `.env`, replace the JWT secret, then run:

```powershell
docker compose --profile app up --build
```

PostgreSQL data and uploaded images are stored in named Docker volumes.

## Configuration

| Environment variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/postwave` | JDBC connection URL |
| `DB_USERNAME` | `postwave` | Database user |
| `DB_PASSWORD` | `postwave` | Database password |
| `JWT_SECRET` | development-only value | Base64 HMAC secret (32+ bytes before encoding) |
| `JWT_EXPIRATION_SECONDS` | `86400` | Token lifetime |
| `UPLOAD_DIRECTORY` | `uploads` | Image storage directory |
| `CORS_ALLOWED_ORIGINS` | local frontend origins | Comma-separated exact origins |

Never use the development JWT secret in a deployed environment.

## Main endpoints

| Method | Path | Authentication |
|---|---|---|
| `POST` | `/api/auth/signup` | Public |
| `POST` | `/api/auth/login` | Public |
| `GET` | `/api/posts?filter=popular` | Public |
| `GET` | `/api/posts/{id}` | Public |
| `POST` | `/api/posts` | Bearer token |
| `PUT` | `/api/posts/{id}` | Owner/admin |
| `DELETE` | `/api/posts/{id}` | Owner/admin |
| `PATCH` | `/api/posts/{id}/wave` | Bearer token |
| `GET` | `/api/posts/{id}/wavers` | Public |

Create and update use `multipart/form-data` fields named `title`, `content`, and optional `image`.

## Architecture talking points

The controller layer owns HTTP mapping and validation, services own transactions and business authorization, repositories own persistence, and response records prevent JPA entities/password hashes from leaking through the API. JWT authentication is implemented as a stateless filter that resolves the current database user into a typed principal. Flyway owns the schema, while Hibernate uses `validate` rather than mutating production tables. The wave relationship is an explicit entity with a composite key, making uniqueness and future metadata such as `created_at` straightforward.
