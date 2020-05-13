FROM tiangolo/meinheld-gunicorn-flask:python3.7

COPY . /app

RUN pip install --upgrade pip
RUN pip install plac \
    tqdm \
    requests
RUN pip install .
