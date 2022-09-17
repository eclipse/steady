# https://hub.docker.com/layers/library/eclipse-temurin/11.0.16.1_1-jre/images/sha256-ba66fe69ca119df55837f1e1185a242912ebb67431b332c67b153a8dbbe42106?context=explore
FROM eclipse-temurin@sha256:ba66fe69ca119df55837f1e1185a242912ebb67431b332c67b153a8dbbe42106

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
