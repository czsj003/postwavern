# PostWave

PostWave is a complete social publishing application rebuilt for full-stack and Java interview preparation. It combines a responsive React client with a production-style Spring Boot API, PostgreSQL persistence, JWT authentication, image uploads, automated tests, Docker packaging, and CI.

## Frontend stack

- React 19, TypeScript, Vite, React Router
- Responsive, accessible custom CSS design
- Typed API client and persistent JWT session
- ESLint and TypeScript build checks
- Multi-stage Node/nginx production image

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
- Authenticated create, edit, delete, and wave/unwave
- Owner/admin authorization for edit and delete
- JPEG, PNG, GIF, and WebP uploads (maximum 5 MB)
- Responsive desktop/mobile UI, recent/popular feeds, sharing, and loading/error/empty states
- Stable JSON error codes and field validation errors
- Database migrations and real PostgreSQL integration tests

## Run locally

Requirements: Java 21, Node.js 22+, and Docker Desktop.

```powershell
docker compose up -d postgres
cd backend
.\mvnw.cmd spring-boot:run
```

In a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

The development UI runs at `http://localhost:5173`; the API runs at `http://localhost:8080`. Health check: `GET /api/health`. The OpenAPI contract is available at `GET /openapi.yaml`.

Run tests (Docker must be running because repository and API tests use Testcontainers):

```powershell
cd backend
.\mvnw.cmd test
```

Run all frontend static checks:

```powershell
cd frontend
npm run check
```

## Run the containerized stack

Copy `.env.example` to `.env`, replace the JWT secret, then run:

```powershell
docker compose --profile app up --build
```

Open `http://localhost:3000`. nginx serves the React SPA and reverse-proxies `/api` and `/uploads` to Spring Boot, so the browser uses one origin. PostgreSQL data and uploaded images are stored in named Docker volumes.

## Project structure

```text
frontend/                       React + TypeScript single-page application
backend/src/main/java/          Spring Boot application and domain packages
backend/src/main/resources/     configuration, Flyway migration, OpenAPI contract
backend/src/test/               unit, MVC, repository, and integration tests
.github/workflows/              full-stack GitHub Actions pipeline
compose.yaml                    PostgreSQL, backend, and frontend services
```

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

The React client separates pages, reusable presentation components, authentication state, and a typed API adapter. Protected routes keep authoring screens behind authentication, while the API remains the source of truth for ownership and authorization.

On the backend, controllers own HTTP mapping and validation, services own transactions and business authorization, and repositories own persistence. Response records prevent JPA entities and password hashes from leaking through the API. JWT authentication is implemented as a stateless filter that resolves the current database user into a typed principal. Flyway owns the schema, while Hibernate uses `validate` rather than mutating production tables. The wave relationship is an explicit entity with a composite key, making uniqueness and future metadata such as `created_at` straightforward.

GitHub Actions runs frontend lint/type/build checks and the complete Maven test suite on every relevant push or pull request. The Docker Compose production path builds immutable frontend/backend images and waits for PostgreSQL health before starting the API.
