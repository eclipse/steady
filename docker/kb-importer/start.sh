#!/bin/bash

mkdir -p /kb-importer/data
cd /kb-importer/data
if [ -f /kb-importer/kb-importer.jar ]
then
  mv /kb-importer/kb-importer.jar /kb-importer/kaybee /kb-importer/data
fi

#substitute env variables in kaybeeconf.yaml (for kaybee) and kb-importer.sh (for cron)
envsubst < ../conf/kaybeeconf.yaml > ../conf/kaybeeconf-eval.yaml
envsubst < ../kb-importer.sh > ../kb-importer-eval.sh

./kaybee update --force

#Adding certs
certs=`ls /kb-importer/certs | grep -v readme.txt`
for cert in $certs; do
   keytool -import -alias $cert -storepass changeit -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts -file /kb-importer/certs/$cert -noprompt
done

#Wait for backend to start
sleep 120

#Run initial importi
./../kb-importer.sh

#create a cron job kaybeeconf.yaml
crontab -l > tmpcron
if ! cat tmpcron | grep "kb-importer.sh"
then
    if [ -z "$KB_IMPORTER_CRON" ]	
    then
      echo "0 0 * * * PATH=$PATH BACKEND_SERVICE_URL=$BACKEND_SERVICE_URL /kb-importer/kb-importer-eval.sh >> /kb-importer/cron.log 2>&1" >> tmpcron
    else
      echo "$KB_IMPORTER_CRON" " PATH=$PATH BACKEND_SERVICE_URL=$BACKEND_SERVICE_URL /kb-importer/kb-importer-eval.sh  >> /kb-importer/cron.log 2>&1" >> tmpcron
    fi
fi
crontab tmpcron
echo "cron job created."
rm tmpcron
cron -f
