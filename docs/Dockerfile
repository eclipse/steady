FROM python:3-alpine

RUN apk --no-cache add git

WORKDIR /tmp

ADD . .

RUN pip install -r requirements.txt

EXPOSE 8000

CMD python docs.py public --mkserve --dev_addr docs:8000