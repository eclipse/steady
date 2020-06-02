#!/bin/bash
set -e
echo "Packaging CLI"

if [ -z $1 ];
then
  echo "You did not specify a release (format: X.Y.Z)"
  exit 1
fi

cd `dirname $0`
VULAS_RELEASE="$1"
mkdir -p /tmp/cli
WORKDIR="/tmp/cli"

mkdir -p  $WORKDIR/steady-cli/app
mkdir -p  $WORKDIR/steady-cli/instr

wget -P $WORKDIR/steady-cli/instr https://repo1.maven.org/maven2/com/sap/research/security/vulas/lang-java/${VULAS_RELEASE}/lang-java-${VULAS_RELEASE}-jar-with-dependencies.jar
wget -O $WORKDIR/steady-cli/steady-cli-${VULAS_RELEASE}-jar-with-dependencies.jar https://repo1.maven.org/maven2/com/sap/research/security/vulas/cli-scanner/${VULAS_RELEASE}/cli-scanner-${VULAS_RELEASE}-jar-with-dependencies.jar
cp vulas-custom.properties.sample $WORKDIR/steady-cli
zip -r $WORKDIR/steady-cli-$VULAS_RELEASE.zip  $WORKDIR/steady-cli/
echo "Done"
