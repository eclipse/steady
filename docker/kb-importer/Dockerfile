# https://hub.docker.com/layers/eclipse-temurin/library/eclipse-temurin/11.0.15_10-jre/images/sha256-1543416e05e9fde8ffede76cd5f0955b640d7159bdbff8574eed6560a98e4ad3?context=explore
FROM eclipse-temurin@sha256:1543416e05e9fde8ffede76cd5f0955b640d7159bdbff8574eed6560a98e4ad3

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        openssl wget tar git cron bash gettext curl \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false

WORKDIR /kb-importer

RUN wget https://github.com/SAP/project-kb/releases/download/v0.6.18/kaybee-0.6.18_linux-amd64 -O kaybee
RUN chmod +x kaybee

COPY kb-importer-$VULAS_RELEASE.jar kb-importer.jar
RUN chmod +x kb-importer.jar

COPY run.sh /kb-importer/
RUN chmod +x /kb-importer/run.sh

EXPOSE 8080

ENTRYPOINT ["sh","/kb-importer/run.sh"]
