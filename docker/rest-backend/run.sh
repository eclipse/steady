#!/bin/bash

FLYWAY_OPTS="-Dflyway.skipDefaultCallbacks=true" 

if [ "x$DELAY_STARTUP" != "x"  ];
then
    echo "Delaying startup by $DELAY_STARTUP seconds to avoid race with other backend instances"
    sleep $DELAY_STARTUP
else
    echo "Starting with no delay"
fi

java \
	-Dhttp.nonProxyHosts=$NON_PROXY_HOSTS \
	-Dhttp.proxyHost=$HTTP_PROXY_HOST \
	-Dhttp.proxyPort=$HTTP_PROXY_PORT \
	-Dhttps.proxyHost=$HTTPS_PROXY_HOST \
	-Dhttps.proxyPort=$HTTPS_PROXY_PORT \
    -Dvulas.jira.usr=$JIRA_USER \
    -Dvulas.jira.pwd=$JIRA_PASSWORD \
    $FLYWAY_OPTS \
    -Dspring.profiles.active=docker \
	-jar /vulas/rest-backend.jar
