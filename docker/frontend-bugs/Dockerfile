FROM jetty:alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ARG VULAS_RELEASE

COPY frontend-bugs-${VULAS_RELEASE}.war $CATALINA_HOME/webapps/bugs.war
