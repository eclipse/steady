#!/bin/bash

# Expects a JAR file and search string as argument
# Searches the file names and the manifest file of the given JAR for the occurence of the search string
# Use as follows: find . -name '*.jar' -exec ./find_jars.sh {} crystaldecisions12 \;

echo ----- $1
jar tf $1 | grep -i $2
jar xf $1 META-INF/MANIFEST.MF
less META-INF/MANIFEST.MF | grep -i $2