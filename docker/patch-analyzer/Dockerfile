FROM openjdk:8-jre-alpine

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

RUN apk add --no-cache subversion

COPY patch-analyzer-$VULAS_RELEASE-jar-with-dependencies.jar /vulas/patch-analyzer.jar

ENTRYPOINT ["java", "-jar", "/vulas/patch-analyzer.jar"]

CMD [""]
