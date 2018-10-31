#!/bin/sh

if [ -z ${POSTGRES_PASSWORD} ] || [ -z ${POSTGRES_USER} ]
then 
    echo 'POSTGRES_USER or POSTGRES_PASSWORD cannot be empty'
    exit 1
fi

postgres