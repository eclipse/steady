FROM postgres:11-alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

USER postgres:postgres

ENV PGDATA	    		    /var/lib/postgresql/data
ENV POSTGRES_DB     		vulas

COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d
COPY run.sh /run.sh

CMD /run.sh

