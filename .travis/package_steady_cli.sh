#!/bin/bash
set -e
echo "Packaging CLI"

if [ -z $1 ];
then
  echo "You did not specify a release (format: X.Y.Z)"
  exit 1
fi

# Commented due to moving from Travis to Eclipse's CI/CD environment
# if [ "$1" != "$TRAVIS_TAG" ]; 
# then
#   echo "Skipping Packaging CLI as this is not a tag release"
#   exit 0 
# fi

cd `dirname $0`
VULAS_RELEASE="$1"
mkdir -p /tmp/cli
WORKDIR="/tmp/cli"

mkdir -p  $WORKDIR/steady-cli/app
mkdir -p  $WORKDIR/steady-cli/instr

curl -o $WORKDIR/steady-cli/instr/lang-java-${VULAS_RELEASE}-jar-with-dependencies.jar https://repo1.maven.org/maven2/org/eclipse/steady/lang-java/${VULAS_RELEASE}/lang-java-${VULAS_RELEASE}-jar-with-dependencies.jar
curl -o $WORKDIR/steady-cli/steady-cli-${VULAS_RELEASE}-jar-with-dependencies.jar      https://repo1.maven.org/maven2/org/eclipse/steady/cli-scanner/${VULAS_RELEASE}/cli-scanner-${VULAS_RELEASE}-jar-with-dependencies.jar
cp steady-custom.properties.sample $WORKDIR/steady-cli
zip -r $WORKDIR/steady-cli-$VULAS_RELEASE.zip $WORKDIR/steady-cli/
echo "Done"
