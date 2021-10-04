#!/bin/bash

REL="3.2.0"
TAG="release-$REL"
INSTALL_DIR="steady-$REL"
SERVICES="ui"

usage () {
    cat <<HELP_USAGE
Installs and prepares the Docker Compose environment of Eclipse Steady.

Requires: bash, curl, docker-compose

Usage: $0 [options...]

 -d, --dir <dir>               Specifies the installation directory (must not exist or be empty)

                               Default: steady-$REL

 -t, --tag <tag|commit>        The tag or commit of source files in https://github.com/eclipse/steady
                                
                               Default: $TAG

 -s, --services <none|core|ui> Docker Compose services to start (default: ui)

                               none - No services at all (corresponds to docker-compose stop)
                               core - Only core services (those required by the Maven/Gradle plugins or the CLI)
                               ui   - All services, incl. the Web interfaces for app and bug mgmt.

 -h, --help                    Prints this help text
HELP_USAGE
    exit 0
}

setup (){
    DIR=$1

    # Services with mounted volumes for configuration and/or data
    conf_services='haproxy postgresql rest-backend cache kb-importer'
    data_services='patch-lib-analyzer postgresql rest-lib-utils cache kb-importer'

    # Create directories
    echo "Creating directory structure in `pwd`/$INSTALL_DIR/ ..."
    mkdir -p $DIR/certs
    for s in $conf_services; do
    mkdir -p $DIR/conf/$s
    done
    for s in $data_services; do
    mkdir -p $DIR/data/$s
    done

    # Download docker-compose.yml and configurations
    echo "Downloading files from https://raw.githubusercontent.com/eclipse/steady/$TAG/docker ..."
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/docker-compose-new.yml -o ./$DIR/docker-compose.yml
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/.env.sample -o ./$DIR/.env
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/haproxy/conf/haproxy.cfg -o ./$DIR/conf/haproxy/haproxy.cfg
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/postgresql/docker-entrypoint-initdb.d/10-vulas-setup.sh -o ./$DIR/conf/postgresql/10-vulas-setup.sh 
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/cache/nginx.conf -o ./$DIR/conf/cache/nginx.conf
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/kb-importer/conf/kaybeeconf.yaml -o ./$DIR/conf/kb-importer/kaybeeconf.yaml
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/rest-backend/conf/restbackend.properties -o ./$DIR/conf/rest-backend/restbackend.properties
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/start-steady.sh -o ./$DIR/start-steady.sh
    chmod 744 ./$DIR/start-steady.sh
}

while true; do
    case "$1" in
        -s | --services ) SERVICES="$2"; shift 2 ;;
        -d | --dir )  INSTALL_DIR="$2"; shift 2 ;;
        -t | --tag )  TAG="$2"; shift 2 ;;
        -h | --help ) usage; shift 2 ;;
        -- ) shift; break ;;
        * ) break ;;
    esac
done

# Install if target dir does not exist or is empty
if [[ ! -d $INSTALL_DIR || -z "$(ls -A $INSTALL_DIR)" ]]; then

    # Check connectivity and tag
    URL="https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/.env.sample"
    RC=`curl -o /dev/null -s -w "%{http_code}\n" $URL`
    if [[ ! $RC == "200" ]]; then
        echo "Cannot reach $URL"
        echo "Check your internet connectivity and/or the tag '$TAG'"
        echo "Aborting installation ..."
        exit 1
    fi

    setup $INSTALL_DIR
    echo "IMPORTANT: Adjust default passwords in ./$INSTALL_DIR/.env"
    read -p "Once done, enter Y to start Steady containers (or any other key to abort): " CONTINUE
    echo "Installation completed"
else
    echo "Installation skipped, because non-empty directory `pwd`/$INSTALL_DIR/ already exists"
    CONTINUE="Y"
fi

# Delegate execution to run-steady.sh
if [[ -f ./$DIR/start-steady.sh && $CONTINUE == 'Y' ]]; then
    if [[ $SERVICES == "ui" || $SERVICES == "core" ]]; then
        echo "Execution delegated to ./$INSTALL_DIR/start-steady.sh ..."
        cd $INSTALL_DIR
        bash start-steady.sh -s $SERVICES
    else
        echo "Execution skipped (services == '$SERVICES')"
    fi
fi
