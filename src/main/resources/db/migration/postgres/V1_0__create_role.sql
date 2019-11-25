CREATE ROLE probation_teams LOGIN PASSWORD '${database_password}';
GRANT USAGE ON SCHEMA probation_teams TO probation_teams;
