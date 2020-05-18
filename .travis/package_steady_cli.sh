#!/bin/bash
set -e
echo "Packaging CLI"

if [ "$VULAS_RELEASE" != "$TRAVIS_TAG" ]; then
 echo "This is not a tag release. No CLI Packaging needed."
 exit 0
fi

if [ -z $1 ];
then
  echo "You did not specify a release (format: X.Y.Z)"
  exit 1
fi

VULAS_RELEASE="$1"
mkdir -p /tmp/cli
WORKDIR="/tmp/cli"

mkdir -p  $WORKDIR/vulas-cli/app
mkdir -p  $WORKDIR/vulas-cli/instr
 
mv ../lang-java/target/lang-java-${VULAS_RELEASE}-jar-with-dependencies.jar $WORKDIR/vulas-cli/instr
mv ../cli-scanner/target/cli-scanner-${VULAS_RELEASE}-jar-with-dependencies.jar $WORKDIR/vulas-cli/steady-cli-${VULAS_RELEASE}-jar-with-dependencies.jar
cp vulas-custom.properties.public.sample $WORKDIR/vulas-cli/vulas-custom.properties.sample
zip -r $WORKDIR/vulas-cli-$VULAS_RELEASE.zip  $WORKDIR/vulas-cli/
echo "Done"

