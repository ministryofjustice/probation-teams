# Copilot instructions

Kotlin / Spring Boot (HMPPS) REST API holding functional mailboxes (FMBs) for probation Local Delivery Units (LDUs) and their teams. Java 25 toolchain, Gradle Kotlin DSL, Postgres (H2 in tests), Flyway.

## Build, test, lint

- Build/test: `./gradlew build` / `./gradlew test`
- Single test class: `./gradlew test --tests "uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnitServiceTest"`; single method: append `.methodName`
- Lint/format (ktlint via the `uk.gov.justice.hmpps.gradle-spring-boot` plugin): `./gradlew ktlintCheck` / `./gradlew ktlintFormat`
- Run locally: `make run` (starts Postgres via docker compose, then `bootRun` on port 8085 with profile `dev`); `make run-skip-docker` if the DB is already up; `make stop` / `make clean`
- Smoke test against a deployed dev namespace: `scripts/smoke-dev.sh` (uses `scripts/probation-teams.sh`, needs kube access; output checked manually)

## Architecture

Package `uk.gov.justice.hmpps.probationteams`: `controllers` → `services` (`LocalDeliveryUnitService`) → `repository` (`LocalDeliveryUnitRepository`, Spring Data JPA) → `model` (`LocalDeliveryUnit` aggregate owning a map of `ProbationTeam`s). `dto` holds API response types; controllers map entities to DTOs in a companion-object function.

- Data hierarchy: probation area code → LDU → team. An FMB can be set at LDU level or team level; setting one for a non-existent LDU creates the LDU. Service methods return `SetOutcome` / `DeleteOutcome` which controllers translate into HTTP responses; `ControllerAdvice` handles errors.
- Input validation is via custom constraint annotations in `model/ValidationConstraints.kt` (`@ProbationAreaCode`, `@LduCode`, `@TeamCode`, `@Email`) applied to service method parameters (service is `@Validated`).
- Security: OAuth2 JWT resource server (`config/ResourceServerConfiguration.kt`) with custom `AuthAwareTokenConverter` (principal from `user_name`, falling back to `client_id`). Authorisation is method-level via `@PreAuthorize` (e.g. `hasAnyRole('VIEW_PROBATION_TEAMS')`). `EndpointSecurityTest` scans all `@RestController`s and fails if an endpoint has no `@PreAuthorize` (class or method) — new endpoints must have one, or be explicitly marked `@PublicEndpoint` / `@ProtectedByIngress` (`resource/ControllerAnnotations.kt`).
- JPA entities are made open via `allOpen` config in `build.gradle.kts` (Entity/MappedSuperclass/Embeddable); `ddl-auto: none` — schema changes go only through Flyway.

## Database / migrations

- Flyway scripts: `src/main/resources/db/migration/general` (shared), `postgres` and `h2` (DB-specific role creation). Test-only seed data lives in `db/test/migration` (V1001+), loaded only by the `h2flyway` profile.
- Runtime app uses schema `probation_teams`; Flyway runs with separate superuser credentials (`SUPERUSER_USERNAME`/`SUPERUSER_PASSWORD`) from the app datasource user.
- Add new migrations to `db/migration/general` with the next `V1_x__` version.

## Testing conventions

- The `test` profile group activates `stdout` and `h2flyway` (in-memory H2 in PostgreSQL mode, `src/test/resources/application-h2flyway.yml`), so tests need no external DB.
- Integration tests use real signed JWTs via `utils/JwtAuthenticationHelper.kt` and `controllers/EntityWithJwtAuthorisationBuilder.kt`; mocking uses MockK / springmockk.

## Deployment

Helm chart in `helm_deploy/` with per-env `values-{dev,preprod,prod}.yaml`; CI is in `.github/workflows/pipeline.yml` (reusable HMPPS workflows). Dockerfile builds the Spring Boot jar; App Insights config in `applicationinsights*.json`.
