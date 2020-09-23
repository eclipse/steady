FROM openjdk:11-jre-slim

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ARG VULAS_RELEASE

COPY patch-lib-analyzer-${VULAS_RELEASE}-jar-with-dependencies.jar /vulas/patch-lib-analyzer.jar
COPY run.sh /vulas/run.sh

RUN chmod +x /vulas/run.sh

CMD ["/vulas/run.sh"]
