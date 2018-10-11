-- Create read-only group
CREATE USER readonly_group WITH
  NOLOGIN
  NOSUPERUSER
  NOINHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_group;

-- Create read-only user
CREATE USER readonly_user WITH
  LOGIN
  ENCRYPTED PASSWORD 'alpaca'
  INHERIT;

GRANT readonly_group TO readonly_user;