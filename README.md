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
| Testing        | JUnit 5, Testcontainers                      |

## Getting started

### Prerequisites

- JDK 25+
- Docker (required by Testcontainers and Docker Compose)

No local Maven installation is needed — use the bundled wrapper (`./mvnw`).

### Build

```bash
./mvnw clean package
```

The executable JAR is created in `target/`.

### Run tests

```bash
./mvnw verify
```

Integration tests start a real PostgreSQL instance in Docker via Testcontainers.

### Run locally

Until Docker Compose is added, the easiest way to run the service is with a
Testcontainers-managed database:

```bash
./mvnw spring-boot:test-run
```

## Project structure

```
src
├── main
│   ├── java/com/authservice/authservice   # application source code
│   └── resources
│       ├── application.properties         # configuration
│       └── db/changelog                   # Liquibase migrations
└── test
    └── java/com/authservice/authservice   # tests
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
