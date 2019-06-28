#!/bin/bash
# Push vulnerability-assessment-tool images to a registry
#
# Usage: push.sh <registry> <username> <vulnerability-assessment-tool-version>
#
# To run this script you should have already generated the JARs
# Read more here: https://github.com/SAP/vulnerability-assessment-tool/blob/master/docker/README.md

set -e

if [[ -z $1 || -z $2 || -z $3 ]];
then
    echo "[-] Usage: push.sh <registry> <username> <vulnerability-assessment-tool-version>"
    exit 1
fi

REGISTRY_HOST=$1
USERNAME=$2
VERSION=$3
SERVICES='generator frontend-apps frontend-bugs haproxy patch-lib-analyzer postgresql rest-backend rest-lib-utils'

docker login $REGISTRY_HOST

VULAS_RELEASE=$VERSION docker-compose build

for service in $SERVICES ;
do
    IMAGE=${REGISTRY_HOST}/${USERNAME}/vulnerability-assessment-tool-$service:${VERSION}
    docker tag vulnerability-assessment-tool-$service:${VERSION} $IMAGE
    docker push ${IMAGE}
done
