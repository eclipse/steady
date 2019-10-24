FROM openjdk:8-jre-alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ARG VULAS_RELEASE

COPY patch-analyzer-$VULAS_RELEASE-jar-with-dependencies.jar /vulas/patch-analyzer.jar

ENTRYPOINT ["java", "-jar", "/vulas/patch-analyzer.jar"]

CMD [""]
