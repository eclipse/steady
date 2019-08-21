FROM jetty:alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ARG VULAS_RELEASE

COPY frontend-bugs-${VULAS_RELEASE}.war $JETTY_BASE/webapps/bugs.war
