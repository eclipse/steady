# https://hub.docker.com/layers/jetty/library/jetty/9.4.46-jdk11-alpine-eclipse-temurin/images/sha256-dcaab143043b8916675f8533700c07310986e94db9ee4f2f6bf336e8befc53e9?context=explore
FROM jetty@sha256:dcaab143043b8916675f8533700c07310986e94db9ee4f2f6bf336e8befc53e9

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

COPY frontend-bugs-${VULAS_RELEASE}.war $JETTY_BASE/webapps/bugs.war
