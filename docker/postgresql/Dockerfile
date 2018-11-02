FROM postgres:11-alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ENV POSTGRES_DB vulas
ENV PGDATA /var/lib/postgresql/data

COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d
