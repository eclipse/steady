FROM openjdk:11-jre-slim

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        openssl wget tar git \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false

WORKDIR /kb-importer
RUN wget https://github.com/SAP/project-kb/releases/download/v0.6.17/kaybee-0.6.17_linux-amd64 -O kaybee
RUN chmod +x kaybee
COPY kb-importer-$VULAS_RELEASE-jar-with-dependencies.jar kb-importer.jar
RUN chmod +x kb-importer.jar
COPY kb-importer.sh start.sh /kb-importer/
ENTRYPOINT ["sh","/kb-importer/start.sh"]
