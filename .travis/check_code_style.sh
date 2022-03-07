#!/bin/bash

# Download Google formatter
executable=google-java-format-1.14.0-all-deps.jar
if [[ ! -f $executable ]]; then
    curl -L https://github.com/google/google-java-format/releases/download/v1.14.0/google-java-format-1.14.0-all-deps.jar --output $executable
    printf "Downloaded [%s]\n" $executable
fi

# Java files below src/main
java_files=java-files.txt
if [[ -f $java_files ]]; then
    rm $java_files
fi
sed -n "s/.*<module>\(.*\)<\/module>.*/\1/p" pom.xml | xargs -I % sh -c 'find %/src/main/java -name *.java >> java-files.txt 2>/dev/null'
sed -n "s/.*<module>\(.*\)<\/module>.*/\1/p" pom.xml | xargs -I % sh -c 'find %/src/test/java -name *.java >> java-files.txt 2>/dev/null'
count=`less $java_files | wc -l`
printf "Found [%s] Java files in all modules' source directories\n" $count

# Check or format
if [[ $1 == "format" ]]; then

    printf "Formatting... "
    java -jar $executable -r --skip-sorting-imports --skip-javadoc-formatting --set-exit-if-changed @java-files.txt
    status=$?

    if [[ $status -eq 0 ]]; then
        printf "all files were compliant with Google Java Style Guide\n"
        exit 0
    else
        printf "some files were NOT compliant with Google Java Style Guide and have been changed\n"
        exit 1
    fi
else
    printf "Checking... "
    java -jar $executable -n --skip-sorting-imports --skip-javadoc-formatting --set-exit-if-changed @java-files.txt > non-compliant-files.txt
    status=$?

    if [[ $status -eq 0 ]]; then
        printf "all files are compliant with Google Java Style Guide\n"
        exit 0
    else
        count_noncompliant=`less non-compliant-files.txt | wc -l`
        printf "[%s] files are NOT compliant with Google Java Style Guide:\n" $count_noncompliant
        cat non-compliant-files.txt
        exit 1
    fi
fi
