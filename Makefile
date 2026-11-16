# Local development helper.
#
# Targets:
#   run              Start missing docker compose dependencies (db, auth), then run the service
#   run-skip-docker  Run the service only (assumes dependencies are already up)
#   stop             Stop the docker compose containers
#   clean            ./gradlew clean and remove containers and volumes (keeps images)
#   update           Update the Gradle wrapper and dependency versions
#   prune            Remove containers, volumes and images
#
# Options:
#   DEBUG=1          Add --debug-jvm to bootRun (e.g. make run DEBUG=1)

SHELL := /bin/bash

.PHONY: run run-skip-docker stop clean update prune

run: docker-up run-skip-docker

# Only creates/starts containers that are missing or stopped; running ones are left untouched
docker-up:
	docker compose -f docker-compose.yml up -d --wait --remove-orphans

run-skip-docker: export SERVER_PORT = 8085
run-skip-docker: export DATABASE_ENDPOINT = localhost
run-skip-docker: export DATABASE_NAME = probation-teams-db
run-skip-docker: export SPRING_DATASOURCE_USERNAME = probation_teams
run-skip-docker: export SPRING_DATASOURCE_PASSWORD = probation_teams
run-skip-docker: export SPRING_DATASOURCE_URL=jdbc:postgresql://$${DATABASE_ENDPOINT}/$${DATABASE_NAME}
run-skip-docker: export SUPERUSER_USERNAME = admin
run-skip-docker: export SUPERUSER_PASSWORD = admin_password
run-skip-docker: export SPRING_PROFILES_ACTIVE = dev
run-skip-docker: export OAUTH_ENDPOINT_URL = http://localhost:8080/auth
run-skip-docker:
	./gradlew bootRun $(if $(DEBUG),--debug-jvm)

stop:
	docker compose stop

clean:
	./gradlew clean
	docker compose down --remove-orphans --volumes

update:
	./gradlew wrapper --gradle-version latest
	./gradlew useLatestVersions

prune:
	docker compose down --remove-orphans --volumes --rmi all
