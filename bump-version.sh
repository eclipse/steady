#!/bin/bash
#
# This file is part of Eclipse Steady.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
#

# Updates the build timestamp and version identifier in files used by Maven, Travis and mkdocs as well as some bash scripts
# Use to bump versions before and after every release

if [ -z $1 ]; then
  printf "usage: set-version.sh NEW-VERSION\n\n"
  printf "Updates the build timestamp and version identifier in files used by Maven, Travis and mkdocs as well as some bash scripts"
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
#new_timestamp=`date --utc --iso-8601=seconds`
new_timestamp=`date -u "+%Y-%m-%dT%H:%M:%S"`+00:00

# Maven
#find . -name pom.xml -exec sed -i "" "0,/${old}/s//${new}/" {} \;
find . -name pom.xml -exec sed -i "" "s/${old}/${new}/" {} \;
sed -i "" "s/${old_timestamp}/${new_timestamp}/" pom.xml

# Travis
sed -i "" "s/${old}/${new}/" .travis/.env

# mkdocs and bash scripts (keep current if new version is snapshot)
if [ -z $is_snap ]; then
  sed -i "" "s/${old_doc}/${new}/" docs/mkdocs.yml
  sed -i "" "s/${old_doc}/${new}/" docs/public.properties
  sed -i "" "s/${old_doc}/${new}/" docker/setup-steady.sh
  sed -i "" "s/${old_doc}/${new}/" docker/start-steady.sh
  sed -i "" "s/${old_doc}/${new}/" docker/.env.sample
fi
