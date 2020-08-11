all: lint build test

build:
	go build ./...

lint:
	find . -name '*.go' | xargs gofmt -w -s

test:
	 go test -cover
