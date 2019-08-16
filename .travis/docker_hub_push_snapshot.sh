#!/bin/bash

set -x

if [[ $VULAS_RELEASE =~ ^([0-9]+\.[0-9]+\.[0-9]+-SNAPSHOT)$ ]]; then
    echo "$DOCKER_HUB_NARAMSIM_PASSWORD" | docker login -u "$DOCKER_HUB_NARAMSIM_USERNAME" --password-stdin
    (cd ../docker && ./push-images.sh -r registry.hub.docker.com -p vulas -v "${VULAS_RELEASE}")
else
    echo '[!] Refusing to push non-snapshot version'
    echo "    VULAS_RELEASE: $VULAS_RELEASE"
fi
