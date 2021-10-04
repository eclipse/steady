#!/bin/bash

REL="3.2.0"
SERVICES="ui"

stop_ui () {
    for service in 'frontend-apps frontend-bugs cache' ; do
        docker-compose -f ./docker-compose.yml stop ${service}
    done
}

usage () {
    cat <<HELP_USAGE
Starts the Docker Compose environment of Eclipse Steady.

Requires: docker-compose

Usage: $0 [options...]

 -s, --services <none|core|ui> Docker Compose services to start (default: ui)

                               none - No services at all (corresponds to docker-compose stop)
                               core - Only core services (those required by the Maven/Gradle plugins or the CLI)
                               ui   - All services, incl. the Web interfaces for app and bug mgmt.

 -h, --help                    Prints this help text
HELP_USAGE
    exit 0
}

core_usage () {
    cat <<HELP_USAGE

Scan your Maven project as follows:

    mvn -Dvulas.shared.backend.serviceUrl=http://localhost:8033/backend org.eclipse.steady:plugin-maven:$REL:app

HELP_USAGE
}

ui_usage () {
    cat <<HELP_USAGE
Point your browser to:

    http://localhost:8033/apps to see the results of your application scans
    http://localhost:8033/bugs to see all vulnerabilities imported from Project KB into Steady's database

HELP_USAGE
}

more_info () {
    cat <<HELP_USAGE
Find more information at https://eclipse.github.io/steady
HELP_USAGE
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
    core)   stop_ui; docker-compose -f ./docker-compose.yml              up -d --build; core_usage,           more_info ;;
    ui)              docker-compose -f ./docker-compose.yml --profile ui up -d --build; core_usage; ui_usage; more_info ;;
esac
