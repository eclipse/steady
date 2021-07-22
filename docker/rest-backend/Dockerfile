FROM openjdk:11-jre-slim

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bash \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false

COPY rest-backend-$VULAS_RELEASE.jar /vulas/rest-backend.jar
COPY run.sh /vulas/run.sh
RUN touch /$VULAS_RELEASE

EXPOSE 8091

RUN chmod +x /vulas/run.sh

CMD ["/vulas/run.sh"]
