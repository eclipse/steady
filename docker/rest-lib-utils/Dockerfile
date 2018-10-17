FROM openjdk:8-jre-alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

ARG VULAS_RELEASE

COPY rest-lib-utils-${VULAS_RELEASE}.jar /vulas/rest-lib-utils.jar
RUN touch /$VULAS_RELEASE

EXPOSE 8092

CMD java -Dhttp.nonProxyHosts=${NON_PROXY_HOSTS} -Dhttps.nonProxyHosts=${NON_PROXY_HOSTS} -Dhttps.proxyHost=${HTTPS_PROXY_HOST} -Dhttps.proxyPort=${HTTP_PROXY_PORT} -Dhttp.proxyHost=${HTTP_PROXY_HOST} -Dhttp.proxyPort=${HTTP_PROXY_PORT} -jar /vulas/rest-lib-utils.jar
