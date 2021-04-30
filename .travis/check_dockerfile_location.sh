#!/bin/bash

SERVICES='frontend-apps frontend-bugs patch-lib-analyzer rest-backend rest-lib-utils'
ERROR=false
FAILED=""

for service in $SERVICES ; do
    if ! { [ -f "./docker/$service/Dockerfile" ] || [ -f "./$service/Dockerfile" ]; }; then
        ERROR=true
        FAILED="$FAILED$service "
    fi
done

if [ "$ERROR" = "true" ]; then
    echo "[-] The following Dockerfiles are not present anymore: docker/$FAILED/Dockerfile"
    exit 1
fi
