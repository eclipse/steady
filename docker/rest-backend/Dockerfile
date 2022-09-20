FROM eclipse-temurin:11.0.16.1_1-jre

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bash \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false

COPY rest-backend-$VULAS_RELEASE.jar /steady/rest-backend.jar
COPY run.sh /steady/run.sh
RUN touch /$VULAS_RELEASE

EXPOSE 8091

RUN chmod +x /steady/run.sh

CMD ["/steady/run.sh"]
