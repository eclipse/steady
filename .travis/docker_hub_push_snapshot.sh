#!/bin/bash
# Should be run from repository's root
# Usage: bash .travis/docker_hub_push_snapshot.sh

if [[ $VULAS_RELEASE =~ ^([0-9]+\.[0-9]+\.[0-9]+-SNAPSHOT)$ ]]; then
    echo "$DOCKER_HUB_SUMEET_PASSWORD" | docker login -u "$DOCKER_HUB_SUMEET_USERNAME" --password-stdin
    if [ -z "$JIB" ]; then
      (cd docker && bash push-images.sh -r docker.io -p eclipse -v "${VULAS_RELEASE}")
    else
      ./.travis/skaffold build -f ./.travis/skaffold.yaml
    fi
else
    echo '[!] Refusing to push non-snapshot version'
    echo "    VULAS_RELEASE: $VULAS_RELEASE"
fi
