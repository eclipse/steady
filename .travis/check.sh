#!/bin/sh

set -e
set -x

sleep 15

N_CONTAINERS_RUNNING=$(docker ps --filter "status=running" -q | wc -l)

if [ ${N_CONTAINERS_RUNNING} -eq 7 ]
then
    exit 0
else
    docker images
    docker ps -as
    exit 1
fi