FROM jetty:alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ARG VULAS_RELEASE

COPY frontend-apps-${VULAS_RELEASE}.war $JETTY_BASE/webapps/apps.war
