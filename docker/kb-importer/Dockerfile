FROM openjdk:8-jre-alpine

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

RUN apk --no-cache add openssl wget tar git
WORKDIR /kb-importer
RUN wget https://github.com/SAP/project-kb/releases/download/v0.6.17/kaybee-0.6.17_linux-amd64 -O kaybee
RUN chmod +x kaybee
COPY kb-importer-$VULAS_RELEASE-jar-with-dependencies.jar kb-importer.jar
RUN chmod +x kb-importer.jar
COPY kb-importer.sh start.sh /kb-importer/
RUN chmod +x /kb-importer/kb-importer.sh /kb-importer/start.sh
ENTRYPOINT ["sh","/kb-importer/start.sh"]
