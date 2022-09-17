# https://hub.docker.com/layers/library/eclipse-temurin/11.0.16.1_1-jre/images/sha256-ba66fe69ca119df55837f1e1185a242912ebb67431b332c67b153a8dbbe42106?context=explore
FROM eclipse-temurin@sha256:ba66fe69ca119df55837f1e1185a242912ebb67431b332c67b153a8dbbe42106

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

COPY rest-lib-utils-${VULAS_RELEASE}.jar /steady/rest-lib-utils.jar
RUN touch /$VULAS_RELEASE

EXPOSE 8092

CMD java -Dhttp.nonProxyHosts=${NON_PROXY_HOSTS} -Dhttps.nonProxyHosts=${NON_PROXY_HOSTS} -Dhttps.proxyHost=${HTTPS_PROXY_HOST} -Dhttps.proxyPort=${HTTP_PROXY_PORT} -Dhttp.proxyHost=${HTTP_PROXY_HOST} -Dhttp.proxyPort=${HTTP_PROXY_PORT} -jar /steady/rest-lib-utils.jar
