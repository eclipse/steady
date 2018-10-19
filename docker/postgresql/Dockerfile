FROM postgres:11-alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d

ENV PGDATA	    		    /var/lib/postgresql/data
ENV POSTGRES_DB     		vulas

