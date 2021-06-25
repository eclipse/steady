#!/bin/bash

if [ ! -f /kb-importer/data/running ]
then
  touch /kb-importer/data/running

  #kaybee update
  cd /kb-importer/data
  ./kaybee update --force

  #run kaybee import for kaybeeconf-eval.yaml (as it contains the substituted env variables for the source repo and branch)
  echo `date` " Running Kaybee Import" 
  ./kaybee pull -c ../conf/kaybeeconf-eval.yaml
# As we cannot configure the destination folder of kaybee pull (for now), we explicitly move the resulting folder to the configured one and skip kaybee merge as we only have 1 source configurable
  mv .kaybee/repositories/github.com_sap.project-kb_$KB_IMPORTER_STATEMENTS_BRANCH/statements $KB_IMPORTER_STATEMENTS_FOLDER
#  echo `date` " Running Kaybee Merge" >> job.log 2>&1
#  ./kaybee merge -s -c ../conf/kaybeeconf.yaml
#  echo `date` " Kaybee Merge Done" >> job.log 2>&1
  ./kaybee export -t steady -c ../conf/kaybeeconf.yaml -f $KB_IMPORTER_STATEMENTS_FOLDER
  chmod +x steady.sh
  ./steady.sh 
  echo `date` " Kaybee Import Done" 
  rm /kb-importer/data/running
else
  echo `date` " Kaybee Import already Running" 
fi
