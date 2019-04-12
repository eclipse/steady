#!/bin/sh

echo -e "\n[+] Building new archives"

( set -x; mvn -U -e -Dhttp.proxyHost=${HTTP_PROXY_HOST} -Dhttp.proxyPort=${HTTP_PROXY_PORT} -Dhttps.proxyHost=${HTTPS_PROXY_HOST} -Dhttps.proxyPort=${HTTPS_PROXY_PORT} ${mvn_flags} clean install)

if [ $? -ne 0 ]; then
	echo -e "\n[!] Couldn't build new archives"
	exit 1
fi

echo -e '\n[+] Cleaning old archives'

rm /exporter/**/*.?ar 2> /dev/null
rm /exporter/client-components/*.?ar 2> /dev/null

VULAS_JAVA_BACKEND_COMPONENTS="frontend-apps frontend-bugs patch-lib-analyzer rest-backend rest-lib-utils"
VULAS_JAVA_CLIENT_COMPONENTS="patch-analyzer cli-scanner plugin-maven plugin-gradle"
VULAS_JAVA_COMPONENTS="cli-scanner frontend-apps frontend-bugs lang-java-reach-wala lang-java-reach lang-java lang-python lang patch-analyzer patch-lib-analyzer plugin-maven repo-client rest-backend rest-lib-utils shared "

echo -e '\n[+] Copying new archives'

for i in $VULAS_JAVA_BACKEND_COMPONENTS ; do
    cp $i/target/*.?ar /exporter/$i/
done

mkdir -p /exporter/client-components/

for i in $VULAS_JAVA_CLIENT_COMPONENTS ; do
    cp $i/target/*.?ar /exporter/client-components/
done

mkdir -p /exporter/all-components/

for i in $VULAS_JAVA_COMPONENTS ; do
    cp $i/target/*.?ar /exporter/all-components/
done

echo -e '\n[+] Done'

sleep 1
