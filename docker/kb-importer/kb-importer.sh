#!/bin/sh

#kaybee update
cd /kb-importer/data
./kaybee update --force

#run kaybee import for kaybeeconf.yaml
if [ -f /kb-importer/data/init ]
then
  echo `date` " Running Kaybee Import"
  ./kaybee pull -c ../conf/kaybeeconf.yaml
  echo `date` " Running Kaybee Merge"
  ./kaybee merge -c ../conf/kaybeeconf.yaml
  echo `date` " Kaybee Merge Done"
  ./kaybee export -t steady -c ../conf/kaybeeconf.yaml
  chmod +x steady.sh
  sh steady.sh
  echo `date` " Kaybee Import Done"
fi
