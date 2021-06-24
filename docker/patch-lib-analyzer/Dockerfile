FROM openjdk:11-jre-slim

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bash \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false

COPY patch-lib-analyzer-${VULAS_RELEASE}-jar-with-dependencies.jar /vulas/patch-lib-analyzer.jar
COPY run.sh /vulas/run.sh

RUN chmod +x /vulas/run.sh

CMD ["/vulas/run.sh"]
