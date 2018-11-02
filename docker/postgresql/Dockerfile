FROM postgres:11-alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ENV POSTGRES_DB     		vulas
ENV PGDATA	    		    /var/lib/postgresql/data

RUN mkdir ${PGDATA} && chown postgres:postgres ${PGDATA}

USER postgres:postgres

COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d
COPY run.sh /run.sh

CMD /run.sh

