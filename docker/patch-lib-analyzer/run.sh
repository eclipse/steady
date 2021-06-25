#!/bin/bash

#Wait for backend to start and kb-importer to insert data
sleep 300

java \
	-Dhttp.nonProxyHosts=$NON_PROXY_HOSTS \
	-Dhttp.proxyHost=$HTTP_PROXY_HOST \
	-Dhttp.proxyPort=$HTTP_PROXY_PORT \
	-Dhttps.proxyHost=$HTTPS_PROXY_HOST \
	-Dhttps.proxyPort=$HTTPS_PROXY_PORT \
    -Dspring.profiles.active=docker \
	-jar /vulas/patch-lib-analyzer.jar $PATCHEVAL_OPTS
