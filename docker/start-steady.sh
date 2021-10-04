#!/bin/bash

SERVICES="ui"

stopui () {
    for service in 'frontend-apps frontend-bugs cache' ; do
        docker-compose -f ./docker-compose.yml stop ${service}
    done
}

usage () {
    cat <<HELP_USAGE
Starts the Docker Compose environment of Eclipse Steady.

Usage: $0 [options...]

 -s, --services <none|core|ui> Docker Compose services to start (default: ui)

                               none - No services at all (corresponds to docker-compose stop)
                               core - Only core services (those required by the Maven/Gradle plugins or the CLI)
                               ui   - All services, incl. the Web interfaces for app and bug mgmt.

 -h, --help                    Prints this help text
HELP_USAGE
    exit 0
}

while true; do
    case "$1" in
        -s | --services ) SERVICES="$2"; shift 2 ;;
        -h | --help ) usage; shift 2 ;;
        -- ) shift; break ;;
        * ) break ;;
    esac
done

# Start different sets of services
case $SERVICES in
    none)   docker-compose -f ./docker-compose.yml stop ;;
    core)   stopui; docker-compose -f ./docker-compose.yml              up -d --build ;;
    ui)             docker-compose -f ./docker-compose.yml --profile ui up -d --build ;;
esac
