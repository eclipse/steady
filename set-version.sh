#!/bin/bash

# Updates the build timestamp and version identifier in files used by Maven, Travis and mkdocs
# Use to bump versions before and after every release

if [ -z $1 ]; then
  printf "usage: set-version.sh NEW-VERSION\n\n"
  printf "Updates the build timestamp and version identifier in files used by Maven, Travis and mkdocs"
  exit 1
fi

new=$1
old=`less pom.xml | grep -m 1 "<version>" | sed -n "s/.*<version>\(.*\)<\/version>.*/\1/p"`
old_doc=`less docs/public.properties | grep -m 1 "PROJECT_VERSION=" | sed -n "s/PROJECT_VERSION=\(.*\)$/\1/p"`

echo $new | grep -qE "\-SNAPSHOT$"
if [ $? -eq 0 ]; then
  is_snap=true
  printf "Version identifier used by Maven and Travis will be updated from [%s] to [%s], the one used by mkdocs is kept at [%s]\n" $old $new $old_doc
else
  printf "Version identifier used by Maven, Travis and mkdocs will be updated from [%s] to [%s]\n" $old $new
fi

# Build timestamp
old_timestamp=`less pom.xml | grep -m 1 "<project.build.outputTimestamp>" | sed -n "s/.*<project.build.outputTimestamp>\(.*\)<\/project.build.outputTimestamp>.*/\1/p"`
new_timestamp=`date --utc --iso-8601=seconds`

# Maven
find . -name pom.xml -exec sed -i "0,/${old}/s//${new}/" {} \;
sed -i "s/${old_timestamp}/${new_timestamp}/" pom.xml

# Travis
sed -i "s/${old}/${new}/" .travis/.env

# mkdocs (keep current if new version is snapshot)
if [ -z $is_snap ]; then
  sed -i "s/${old_doc}/${new}/" docs/mkdocs.yml
  sed -i "s/${old_doc}/${new}/" docs/public.properties
fi

# Kubernetes doc files
find kubernetes -name README.md -exec sed -i "s/${old}/${new}/" {} \;
