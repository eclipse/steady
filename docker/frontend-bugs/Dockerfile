FROM tomcat:9-slim

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ARG VULAS_RELEASE

ADD conf/settings.xml $CATALINA_HOME/conf/settings.xml
ADD conf/tomcat-users.xml $CATALINA_HOME/conf/tomcat-users.xml

COPY frontend-bugs-${VULAS_RELEASE}.war $CATALINA_HOME/webapps/bugs.war
