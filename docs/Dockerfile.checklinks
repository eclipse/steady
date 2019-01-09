FROM golang:alpine

RUN apk --no-cache add git

RUN go get -u github.com/raviqqe/muffet

WORKDIR /tmp

ADD checklinks.sh checklinks.sh

CMD ./checklinks.sh docs:8000