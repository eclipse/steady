#!/bin/sh

set -e
set -x

sleep 15

N_CONTAINERS_RUNNING=$(docker ps --filter "status=running" | grep vulnerability-assessment-tool | awk '{print $1}' | wc -l)

if [ ${N_CONTAINERS_RUNNING} -eq 7 ]
then
    exit 0
else
    exit 1
fi