# Auth-Service

![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-wrapper-C71A36?logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql&logoColor=white)

Authentication and account management microservice: registration with email confirmation,
login with JWT access tokens, password change and password reset.

> **Status:** work in progress 🚧

---

## Table of contents

- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Project structure](#project-structure)
- [Development workflow](#development-workflow)

---

## Tech stack

| Area           | Technology                                   |
|----------------|----------------------------------------------|
| Language       | Java 25                                      |
| Framework      | Spring Boot 4.1 (Web MVC)                    |
| Build tool     | Maven (via wrapper)                          |
| Persistence    | Spring Data JPA, PostgreSQL 18, Liquibase    |
| Security       | Spring Security, OAuth2 Resource Server, JWT |
| API docs       | springdoc-openapi (Swagger UI)               |
| Observability  | Spring Boot Actuator, Micrometer, Prometheus |
| Testing        | JUnit 6, Testcontainers, JaCoCo              |

## Getting started

### Prerequisites

- JDK 25+
- Docker (required by Testcontainers and Docker Compose)

No local Maven installation is needed — use the bundled wrapper (`./mvnw`).

### Build

```bash
./mvnw clean package -DskipTests
```

The executable JAR is created in `target/`.

### Run tests

```bash
./mvnw verify
```

- Unit tests (`*Test`) run with Surefire, integration tests (`*IT`) with Failsafe.
- Integration tests start a real PostgreSQL instance in Docker via Testcontainers.
- The coverage report is generated at `target/site/jacoco/index.html`.

Run only the fast unit tests with `./mvnw test`.

### Run locally

1. Create your local environment file and set a database password:

   ```bash
   cp .env.example .env
   ```

   `.env` is git-ignored — never commit it. It is read by both Docker Compose and the `dev` profile.

2. Start PostgreSQL:

   ```bash
   docker compose up -d
   ```

3. Run the application with the `dev` profile:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   In IntelliJ IDEA, set **Active profiles** to `dev` in the run configuration.

Stop the database with `docker compose down` (add `-v` to also delete its data).

### Profiles

| Profile | Purpose                | Database connection                                  |
|---------|------------------------|------------------------------------------------------|
| `dev`   | Local development      | Docker Compose, credentials from `.env`              |
| `test`  | Integration tests      | Testcontainers                                       |
| `prod`  | Production             | Environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |

The application refuses to start without an active profile. In production set
`SPRING_PROFILES_ACTIVE=prod`. Swagger UI is enabled only in the `dev` profile.

## Project structure

```
src
├── main
│   ├── java/com/authservice               # application source code
│   └── resources
│       ├── application.properties         # shared configuration
│       ├── application-{dev,test,prod}.properties  # profile-specific configuration
│       └── db/changelog                   # Liquibase migrations
└── test
    └── java/com/authservice               # unit (*Test) and integration (*IT) tests
compose.yaml                               # local PostgreSQL
.env.example                               # template for local .env
```

## Development workflow

```
feature branch ──PR (squash)──▶ develop ──PR (senior review, merge commit)──▶ main
```

| Branch      | Purpose                                                       |
|-------------|---------------------------------------------------------------|
| `main`      | Reviewed, stable code. Updated only from `develop`.           |
| `develop`   | Integration branch. All feature branches are merged here.     |
| `<type>/…`  | Short-lived work branch created from `develop`.               |

- Every change starts from an up-to-date `develop` and goes through a Pull Request.
- Feature → `develop`: **squash and merge**, branch deleted afterwards.
- `develop` → `main`: reviewed by a senior developer, merged with a **merge commit**.
- CI must pass before any merge.
- Branch naming: `<type>/<short-description>`, e.g. `feat/user-registration`.
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/):
  `feat`, `fix`, `docs`, `refactor`, `test`, `build`, `ci`, `chore`.
