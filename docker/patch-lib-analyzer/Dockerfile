FROM openjdk:8-jre-alpine

LABEL maintainer="Antonino Sabetta antonino.sabetta@sap.com"

ARG VULAS_RELEASE

COPY patch-lib-analyzer-${VULAS_RELEASE}-jar-with-dependencies.jar /vulas/vulas-patcheval.jar
COPY run.sh /vulas/run.sh

RUN chmod +x /vulas/run.sh

CMD ["/vulas/run.sh"]
