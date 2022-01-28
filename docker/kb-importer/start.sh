#!/bin/bash

mkdir -p /kb-importer/data
cd /kb-importer/data
if [ -d $KB_IMPORTER_CLONE_FOLDER ] && [ ! -z $KB_IMPORTER_CLONE_FOLDER ]; then
  mkdir -p $KB_IMPORTER_CLONE_FOLDER
fi
if [ -f /kb-importer/kb-importer.jar ]; then
  mv /kb-importer/kb-importer.jar /kb-importer/kaybee /kb-importer/data
fi

#substitute env variables used by kaybee in kaybeeconf.yaml
sed "s|KB_IMPORTER_STATEMENTS_REPO|$KB_IMPORTER_STATEMENTS_REPO|g" ../conf/kaybeeconf.yaml.sample > ../conf/kaybeeconf.yaml
sed -i "s|KB_IMPORTER_STATEMENTS_BRANCH|$KB_IMPORTER_STATEMENTS_BRANCH|g" ../conf/kaybeeconf.yaml

echo "Statements repo: " $KB_IMPORTER_STATEMENTS_REPO
echo "Statements branch: " $KB_IMPORTER_STATEMENTS_BRANCH
echo "Statements folder: " $KB_IMPORTER_STATEMENTS_FOLDER
echo "Clones folder: " $KB_IMPORTER_CLONE_FOLDER
echo "Skip clones: " $KB_IMPORTER_SKIP_CLONE

./kaybee update --force

#Adding certs
certs=`ls /kb-importer/certs | grep -v readme.txt`
for cert in $certs; do
   keytool -import -alias $cert -storepass changeit -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts -file /kb-importer/certs/$cert -noprompt
done

#Wait for backend to start
sleep 40

#Run initial import
./../kb-importer.sh

#create a cron job kaybeeconf.yaml
crontab -l > tmpcron
if ! cat tmpcron | grep "kb-importer.sh"
then
    if [ -z "$KB_IMPORTER_CRON_HOUR" ]	
    then
      echo "0 0 * * * PATH=$PATH BACKEND_SERVICE_URL=$BACKEND_SERVICE_URL KB_IMPORTER_STATEMENTS_FOLDER=$KB_IMPORTER_STATEMENTS_FOLDER KB_IMPORTER_STATEMENTS_BRANCH=$KB_IMPORTER_STATEMENTS_BRANCH KB_IMPORTER_STATEMENTS_REPO=$KB_IMPORTER_STATEMENTS_REPO KB_IMPORTER_CLONE_FOLDER=$KB_IMPORTER_CLONE_FOLDER KB_IMPORTER_SKIP_CLONE=$KB_IMPORTER_SKIP_CLONE /kb-importer/kb-importer.sh >> /kb-importer/cron.log 2>&1" >> tmpcron
    else
      echo "0 " "$KB_IMPORTER_CRON_HOUR" " * * * PATH=$PATH BACKEND_SERVICE_URL=$BACKEND_SERVICE_URL KB_IMPORTER_STATEMENTS_FOLDER=$KB_IMPORTER_STATEMENTS_FOLDER KB_IMPORTER_STATEMENTS_BRANCH=$KB_IMPORTER_STATEMENTS_BRANCH KB_IMPORTER_STATEMENTS_REPO=$KB_IMPORTER_STATEMENTS_REPO KB_IMPORTER_CLONE_FOLDER=$KB_IMPORTER_CLONE_FOLDER KB_IMPORTER_SKIP_CLONE=$KB_IMPORTER_SKIP_CLONE /kb-importer/kb-importer.sh  >> /kb-importer/cron.log 2>&1" >> tmpcron
    fi
fi
crontab tmpcron
echo "cron job created."
rm tmpcron
cron -f
