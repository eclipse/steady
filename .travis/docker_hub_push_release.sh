#!/bin/bash

if [ "$VULAS_RELEASE" == "$TRAVIS_TAG" ]; then
    echo "$DOCKER_HUB_NARAMSIM_PASSWORD" | docker login -u "$DOCKER_HUB_NARAMSIM_USERNAME" --password-stdin
    (cd ../docker && ./push-images.sh -r registry.hub.docker.com -p vulas -v "$TRAVIS_TAG")
else
    echo '[-] VULAS_RELEASE and Git tag mismatch'
    echo "    VULAS_RELEASE: ${VULAS_RELEASE}"
    echo "    Git tag: ${TRAVIS_TAG}"
    exit 1
fi
