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


# Expects a JAR file and search string as argument
# Searches the file names and the manifest file of the given JAR for the occurence of the search string
# Use as follows: find . -name '*.jar' -exec ./find_jars.sh {} crystaldecisions12 \;

echo ----- $1
jar tf $1 | grep -i $2
jar xf $1 META-INF/MANIFEST.MF
less META-INF/MANIFEST.MF | grep -i $2