# utils: An utility module to manage the vulnerability-assessment-tool helm chart

**Warning**: This module uses helm 3 to manage the helm chart.

A CLI that is meant to help automatically manage the vulnerability-assessment-tool helm chart by allowing for the following features:
-   Upgrading releases with database schema changes
-   Configure the admin chart to serve a specific release
-   Load up data into the vulnerability database (either by dumps or manual)

## Installation
You can either download a release or assuming you already have a recent version of Go installed, pull down the code with `go get`:
```sh
go get -u github.com/ichbinfrog/vulnerability-assessment-tool/kubernetes/helm/utils
```

## Building the utils package from source

```sh
go build -o bin/utils main.go
chmod +x bin/utils
mv bin/utils /usr/local/bin/utils
```
