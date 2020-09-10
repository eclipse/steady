FROM jetty:alpine

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

COPY frontend-bugs-${VULAS_RELEASE}.war $JETTY_BASE/webapps/bugs.war
