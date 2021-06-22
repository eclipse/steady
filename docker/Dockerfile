FROM maven:3-adoptopenjdk-11

LABEL maintainer="steady-dev@eclipse.org"

WORKDIR /vulas

ARG http_proxy
ARG https_proxy

RUN apt-get update

RUN apt-get install -y ca-certificates wget && update-ca-certificates

RUN apt-get install -y python3 python3-pip git
    
RUN python3 -m pip install --upgrade pip setuptools && \
    python3 -m pip install requests virtualenv

RUN if [ ! -e /usr/local/bin/pip ]; then ln -s pip3    /usr/local/bin/pip ; fi && \
    if [ ! -e /usr/bin/python ];    then ln -s python3 /usr/bin/python; fi

ENV ANT_OPTS="-Dhttp.proxyHost=${HTTP_PROXY_HOST} -Dhttp.proxyPort=${HTTP_PROXY_PORT}"

RUN pip install requests virtualenv

COPY . .

COPY docker/run.sh run.sh

RUN chmod +x run.sh

CMD ./run.sh
