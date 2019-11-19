CREATE ROLE probation_teams LOGIN PASSWORD '${database_password}';
GRANT USAGE ON SCHEMA "PROBATION_TEAMS" TO probation_teams;
