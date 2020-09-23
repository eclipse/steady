FROM openjdk:11-jre-slim

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ARG VULAS_RELEASE

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        subversion \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false

COPY patch-analyzer-$VULAS_RELEASE-jar-with-dependencies.jar /vulas/patch-analyzer.jar

ENTRYPOINT ["java", "-jar", "/vulas/patch-analyzer.jar"]

CMD [""]
