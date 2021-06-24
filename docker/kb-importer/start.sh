#!/bin/sh

#kaybee update
mkdir -p /kb-importer/data
cd /kb-importer/data
cp /kb-importer/kb-importer.jar /kb-importer/kaybee /kb-importer/data
./kaybee update --force

#Adding certs
certs=`ls /kb-importer/certs | grep -v readme.txt`
for cert in $certs; do
   keytool -import -alias $cert -storepass changeit -keystore /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/cacerts -file /kb-importer/certs/$cert -noprompt
done

#run kaybee import for kaybeeconf.yaml
if [ ! -f /kb-importer/data/init ]
then
  echo `date` " Running Initial Kaybee Import"
  #wait for the backend to start
  sleep 120
  ./kaybee pull -v -c ../conf/kaybeeconf.yaml
  echo `date` " Running Kaybee Merge"
  ./kaybee merge -s -v -c ../conf/kaybeeconf.yaml
  echo `date` " Kaybee Merge Done"
  ./kaybee export -v -t steady -c ../conf/kaybeeconf.yaml
  chmod +x steady.sh
  sh steady.sh
  touch /kb-importer/data/init
  echo `date` " Kaybee Import Done"
fi

#create a cron job kaybeeconf.yaml
crontab -l > tmpcron
if ! cat tmpcron | grep "kb-importer.sh"
then
    if [ -z "$KB_IMPORTER_CRON" ]
    then
      echo "0 0 * * * /kb-importer/kb-importer.sh" >> tmpcron
    else
      echo "$KB_IMPORTER_CRON" " /kb-importer/kb-importer.sh" >> tmpcron
    fi
fi
crontab tmpcron
rm tmpcron
crond -f
