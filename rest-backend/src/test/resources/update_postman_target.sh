#!/bin/bash

if [ -z ${1+x} ];
then
	echo "Please provide the URL (w/o scheme, with port) of the Vulas backend to be used in the Postman collection, e.g., localhost:8091";
else
	echo "[$1] will be used as URL of the Vulas backend in the Postman collection";
	sed "s/localhost:8091/$1/" vulas3.postman_collection.json > vulas3.postman_collection_alt.json
fi
