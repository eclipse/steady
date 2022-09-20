FROM eclipse-temurin:11.0.16.1_1-jre

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bash \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false

COPY patch-lib-analyzer-${VULAS_RELEASE}-jar-with-dependencies.jar /steady/patch-lib-analyzer.jar
COPY run.sh /steady/run.sh

RUN chmod +x /steady/run.sh

CMD ["/steady/run.sh"]
