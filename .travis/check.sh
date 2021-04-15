#!/bin/sh

set -e
set -x

sleep 15

N_CONTAINERS_RUNNING=$(docker ps --filter "status=running" | grep steady | awk '{print $1}' | wc -l)
REST_BACKEND_CREATED=$(docker images | grep steady-rest-backend | awk '{print $1}' | wc -l)

if [ "${N_CONTAINERS_RUNNING}" -eq 11 ] && [ "${REST_BACKEND_CREATED}" -eq 1 ]
then
    exit 0
else
    exit 1
fi
