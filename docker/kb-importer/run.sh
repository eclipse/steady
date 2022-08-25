#!/bin/bash

# Home directory of kb-importer
home="/kb-importer"

mkdir -p $home/data

echo "Statements repo:   " $KB_IMPORTER_STATEMENTS_REPO
echo "Statements branch: " $KB_IMPORTER_STATEMENTS_BRANCH
echo "Statements folder: " $KB_IMPORTER_STATEMENTS_FOLDER
echo "Skip clones:       " $KB_IMPORTER_SKIP_CLONE

# Substitute env variables used by kaybee in kaybeeconf.yaml
sed    "s|KB_IMPORTER_STATEMENTS_REPO|$KB_IMPORTER_STATEMENTS_REPO|g"     $home/conf/kaybeeconf.yaml.sample > $home/conf/kaybeeconf.yaml
sed -i "s|KB_IMPORTER_STATEMENTS_BRANCH|$KB_IMPORTER_STATEMENTS_BRANCH|g" $home/conf/kaybeeconf.yaml

# Adding certs
certs=`ls $home/certs | grep -v readme.txt`
for cert in $certs; do
   keytool -import -alias $cert -storepass changeit -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts -file $home/certs/$cert -noprompt
done

java -Dvulas.shared.backend.header.X-Vulas-Client-Token=$BACKEND_BUGS_TOKEN \
          -Dvulas.shared.cia.serviceUrl=$CIA_SERVICE_URL \
          -Dvulas.shared.backend.serviceUrl=$BACKEND_SERVICE_URL \
          -jar $home/kb-importer.jar | tee $home/data/analyzer_logs.txt
