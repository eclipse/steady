#!/bin/sh
set -e

if [ -z ${POSTGRES_PASSWORD} ] || [ -z ${POSTGRES_USER} ]
then 
    echo 'POSTGRES_USER or POSTGRES_PASSWORD cannot be empty'
    exit 1
fi

psql --dbname=vulas -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL_B
CREATE TABLE schema_version (
installed_rank integer NOT NULL,
version character varying(50),
description character varying(200) NOT NULL,
type character varying(20) NOT NULL,
script character varying(1000) NOT NULL,
checksum integer,
installed_by character varying(100) NOT NULL,
installed_on timestamp without time zone DEFAULT now() NOT NULL,
execution_time integer NOT NULL,
success boolean NOT NULL
);
ALTER TABLE schema_version OWNER TO postgres;
INSERT INTO schema_version VALUES (1, '20161206.1800', 'Base version', 'BASELINE', 'Base version', NULL, 'postgres', '2016-12-06 18:00:00.000000', 0, true);
EOSQL_B
