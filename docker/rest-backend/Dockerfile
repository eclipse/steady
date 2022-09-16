# https://hub.docker.com/layers/eclipse-temurin/library/eclipse-temurin/11.0.15_10-jre/images/sha256-1543416e05e9fde8ffede76cd5f0955b640d7159bdbff8574eed6560a98e4ad3?context=explore
FROM eclipse-temurin@sha256:1543416e05e9fde8ffede76cd5f0955b640d7159bdbff8574eed6560a98e4ad3

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
