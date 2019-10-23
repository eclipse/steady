#!/bin/bash

build() {
  PLATFORMS="darwin/amd64" # amd64 only as of go1.5
  PLATFORMS="$PLATFORMS linux/amd64 linux/386 windows/amd64"

  SCRIPT_NAME=`basename "$0"`
  FAILURES=""
  SOURCE_FILE=`echo $@ | sed 's/\.go//'`
  CURRENT_DIRECTORY=${PWD##*/}
  OUTPUT=${SOURCE_FILE:-$CURRENT_DIRECTORY} # if no src file given, use current dir name

  type setopt >/dev/null 2>&1

  for PLATFORM in $PLATFORMS; do
    GOOS=${PLATFORM%/*}
    GOARCH=${PLATFORM#*/}
    BIN_FILENAME="${OUTPUT}-${GOOS}-${GOARCH}"
    if [[ "${GOOS}" == "windows" ]]; then BIN_FILENAME="${BIN_FILENAME}.exe"; fi
    CMD="GOOS=${GOOS} GOARCH=${GOARCH} go build -o ${BIN_FILENAME} $@"
    echo "${CMD}"
    eval $CMD || FAILURES="${FAILURES} ${PLATFORM}"

    mv $BIN_FILENAME $(pwd)/bin/$BIN_FILENAME
  done

  # eval errors
  if [[ "${FAILURES}" != "" ]]; then
    echo ""
    echo "${SCRIPT_NAME} failed on: ${FAILURES}"
    exit 1
  fi
}

build
