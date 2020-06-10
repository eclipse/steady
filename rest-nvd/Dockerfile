FROM tiangolo/meinheld-gunicorn-flask:python3.7

LABEL maintainer="Vulas vulas-dev@listserv.sap.com"

WORKDIR /app

RUN pip install --upgrade pip
RUN pip install plac \
    tqdm \
    requests

COPY . /app

RUN pip install .
