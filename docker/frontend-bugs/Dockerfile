FROM jetty:9.4.49-jdk11-eclipse-temurin

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

COPY frontend-bugs-${VULAS_RELEASE}.war $JETTY_BASE/webapps/bugs.war
