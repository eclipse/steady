FROM maven:3-jdk-8-alpine

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

WORKDIR /vulas

COPY . .

ARG http_proxy
ARG https_proxy

RUN apk update && apk add ca-certificates wget && update-ca-certificates

RUN wget -P /tmp https://services.gradle.org/distributions/gradle-4.10.2-bin.zip && \
    mkdir -p /opt/gradle && \
    unzip -d /opt/gradle /tmp/gradle-4.10.2-bin.zip && \
    chmod +x /opt/gradle/gradle-4.10.2/bin/gradle && \
    ln -s /opt/gradle/gradle-4.10.2/bin/gradle /usr/bin/gradle
RUN mkdir -p plugin-gradle/.gradle_home ; \
    chmod +x plugin-gradle/gradlew

RUN apk add --no-cache python3 git && \
    python3 -m ensurepip && \
    rm -r /usr/lib/python*/ensurepip && \
    pip3 install --upgrade pip setuptools && \
    if [ ! -e /usr/bin/pip ]; then ln -s pip3 /usr/bin/pip ; fi && \
    if [[ ! -e /usr/bin/python ]]; then ln -sf /usr/bin/python3 /usr/bin/python; fi && \
    rm -r /root/.cache

ENV ANT_OPTS="-Dhttp.proxyHost=${HTTP_PROXY_HOST} -Dhttp.proxyPort=${HTTP_PROXY_PORT}"

RUN wget -O ./plugin-gradle/gradle/wrapper/gradle.zip http://downloads.gradle.org/distributions/gradle-4.4-bin.zip 

RUN pip install requests

COPY docker/run.sh run.sh

RUN chmod +x run.sh

CMD ./run.sh
