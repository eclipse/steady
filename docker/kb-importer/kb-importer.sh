#!/bin/sh

if [ ! -f /kb-importer/data/running ]
then
  touch /kb-importer/data/running

  #kaybee update
  cd /kb-importer/data
  ./kaybee update --force

  #run kaybee import for kaybeeconf.yaml
  echo `date` " Running Kaybee Import" >> job.log 2>&1
  ./kaybee pull -c ../conf/kaybeeconf.yaml
#  echo `date` " Running Kaybee Merge" >> job.log 2>&1
#  ./kaybee merge -s -c ../conf/kaybeeconf.yaml
#  echo `date` " Kaybee Merge Done" >> job.log 2>&1
  ./kaybee export -t steady -c ../conf/kaybeeconf.yaml -f .kaybee/repositories/github.com_sap.project-kb_vulnerability-data/statements/
  chmod +x steady.sh
  sh steady.sh >> job.log 2>&1
  echo `date` " Kaybee Import Done" >> job.log 2>&1
  rm /kb-importer/data/running
else
  echo `date` " Kaybee Import already Running" >> job.log 2>&1
fi
