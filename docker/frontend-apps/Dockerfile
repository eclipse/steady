FROM jetty:alpine

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

COPY frontend-apps-${VULAS_RELEASE}.war $JETTY_BASE/webapps/apps.war
