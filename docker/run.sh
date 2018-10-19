#!/bin/sh

echo 'Building new archives'
mvn -U -e -Dhttp.proxyHost=${HTTP_PROXY_HOST} -Dhttp.proxyPort=${HTTP_PROXY_PORT} -Dhttps.proxyHost=${HTTPS_PROXY_HOST} -Dhttps.proxyPort=${HTTPS_PROXY_PORT} -DskipTests clean install

echo 'Cleaning old archives'
rm /exporter/**/*.?ar

VULAS_JAVA_COMPONENTS="frontend-apps frontend-bugs patch-lib-analyzer rest-backend rest-lib-utils"

echo 'Copying new archives'
for i in $VULAS_JAVA_COMPONENTS ; do
    cp $i/target/*.?ar /exporter/$i/
done

sleep 2
