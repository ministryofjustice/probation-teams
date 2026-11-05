# probation-teams
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fprobation-teams)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#probation-teams "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/probation-teams/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/probation-teams)
[![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://probation-teams-dev.prison.service.justice.gov.uk/swagger-ui/index.html)

Probation Team Contact and Reference Service.

A Kotlin / Spring Boot REST API that stores and serves the functional mailbox (FMB) email addresses for probation
Local Delivery Units (LDUs) and the teams within them.

Data is organised as **probation area → local delivery unit (LDU) → team**. A functional mailbox can be attached
to an LDU or to an individual team within an LDU.

## Tech stack

- Kotlin, Spring Boot, Gradle (Kotlin DSL), Java 25 toolchain
- PostgreSQL (in-memory H2 for tests), schema managed with Flyway
- OAuth2 JWT resource server (HMPPS Auth)
- OpenAPI / Swagger UI via springdoc

## API

Full, interactive documentation is available in Swagger UI (see the badge above, or `/swagger-ui/index.html` on a running instance).

| Method | Path | Description |
| ------ | ---- | ----------- |
| GET | `/probation-area-codes` | List probation area codes |
| GET | `/local-delivery-units` | List all LDUs |
| GET | `/probation-areas/{probationAreaCode}` | List the LDUs in a probation area |
| GET | `/probation-areas/{probationAreaCode}/local-delivery-units/{lduCode}` | Get a single LDU |
| PUT / DELETE | `/probation-areas/{probationAreaCode}/local-delivery-units/{lduCode}/functional-mailbox` | Set / delete an LDU's FMB |
| PUT / DELETE | `/probation-areas/{probationAreaCode}/local-delivery-units/{lduCode}/teams/{teamCode}/functional-mailbox` | Set / delete a team's FMB |

All endpoints require a valid JWT carrying the `ROLE_VIEW_PROBATION_TEAMS` role. Health and info endpoints
(`/health`, `/info`) and the API docs are public.

## Running locally

Prerequisites: JDK 25 (see `gradle/wrapper/.sdkmanrc` for the SDKMAN version hint), Docker, and `make`.

```sh
make run              # start Postgres via docker compose, then run the service on http://localhost:8085
make run DEBUG=1      # as above, with a JVM debugger attached (--debug-jvm)
make run-skip-docker  # run the service only (assumes Postgres is already running)
make stop             # stop the docker compose containers
make clean            # ./gradlew clean and remove containers and volumes
make prune            # remove containers, volumes and images
make update           # update the Gradle wrapper and dependency versions
```

`make run` uses the `dev` Spring profile and the connection settings defined in the `Makefile`
(database `probation-teams-db` on `localhost:5432`). JWTs are validated against `OAUTH_ENDPOINT_URL`
(default `http://localhost:8080/auth`), so you will need an HMPPS Auth instance there to obtain tokens.

## Building and testing

```sh
./gradlew build                  # compile, test and lint
./gradlew test                   # run all tests
./gradlew test --tests "uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnitServiceTest"   # a single test class
./gradlew ktlintCheck            # lint
./gradlew ktlintFormat           # auto-format
```

Tests run with the `test` profile against an in-memory H2 database (PostgreSQL mode), so no external services are needed.
Test seed data lives in `src/main/resources/db/test/migration`.

## Database migrations

Flyway migrations are in `src/main/resources/db/migration`:

- `general/` – schema migrations shared by all databases
- `postgres/`, `h2/` – database-specific setup (creating the application role)

Hibernate does not manage the schema (`ddl-auto: none`); add a new `V1_x__description.sql` file to `general/` for any change.

## Exercising a deployed environment

The scripts in `scripts/` call the service through `kubectl` and `jq`. See [scripts/README.md](scripts/README.md) for details.
You need `kubectl` access to the relevant namespace (`licences-dev`, `licences-preprod` or `licences-prod`), and
the scripts should be run from the `scripts` directory.

Run `./probation-teams.sh` without arguments for usage. Examples against dev:

```sh
cd scripts

# list the FMBs in the N02 probation area
./probation-teams.sh -ns dev -pa N02

# list the FMBs in LDU N02ALL in probation area N02
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL

# set (create or update) an LDU's FMB
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -update b@c.com

# delete an LDU's FMB
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -delete

# set / delete a team's FMB
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -team N02AZR -update a@b.com
./probation-teams.sh -ns dev -pa N02 -ldu N02ALL -team N02AZR -delete
```

Pipe `GET` output into `jq` for something more readable.

`scripts/smoke-dev.sh` runs this whole sequence (list, update, delete and re-add at both LDU and team level)
against dev. You must check its output manually:

```sh
cd scripts && ./smoke-dev.sh
```

## Deployment

The GitHub Actions pipeline (`.github/workflows/pipeline.yml`) lints the Helm chart, builds and tests the app,
builds the Docker image, and deploys to `dev`, then `preprod`, then `prod`. Helm configuration is in
`helm_deploy/` (`values-{dev,preprod,prod}.yaml`); see [helm_deploy/README.md](helm_deploy/README.md) for
useful Helm commands and TLS certificate notes.
