FROM maven:3-adoptopenjdk-11

LABEL maintainer="steady-dev@eclipse.org"

RUN apt-get update && \
    apt-get install -y python3 python3-pip

RUN python3 -m pip install --upgrade pip setuptools && \
    python3 -m pip install requests virtualenv

RUN if [ ! -e /usr/local/bin/pip ]; then ln -s pip3    /usr/local/bin/pip ; fi && \
    if [ ! -e /usr/bin/python ];    then ln -s python3 /usr/bin/python; fi

ENTRYPOINT ["/usr/local/bin/mvn-entrypoint.sh"]

CMD ["mvn"]
