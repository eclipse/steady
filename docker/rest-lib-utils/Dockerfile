# https://hub.docker.com/layers/eclipse-temurin/library/eclipse-temurin/11.0.15_10-jre/images/sha256-1543416e05e9fde8ffede76cd5f0955b640d7159bdbff8574eed6560a98e4ad3?context=explore
FROM eclipse-temurin@sha256:1543416e05e9fde8ffede76cd5f0955b640d7159bdbff8574eed6560a98e4ad3

LABEL maintainer="steady-dev@eclipse.org"

ARG VULAS_RELEASE

COPY rest-lib-utils-${VULAS_RELEASE}.jar /steady/rest-lib-utils.jar
RUN touch /$VULAS_RELEASE

EXPOSE 8092

CMD java -Dhttp.nonProxyHosts=${NON_PROXY_HOSTS} -Dhttps.nonProxyHosts=${NON_PROXY_HOSTS} -Dhttps.proxyHost=${HTTPS_PROXY_HOST} -Dhttps.proxyPort=${HTTP_PROXY_PORT} -Dhttp.proxyHost=${HTTP_PROXY_HOST} -Dhttp.proxyPort=${HTTP_PROXY_PORT} -jar /steady/rest-lib-utils.jar
