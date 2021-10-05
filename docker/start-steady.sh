#!/bin/bash

REL="3.2.0"
SERVICES="ui"
DC_REQUIRED="1.28"

stop_ui () {
    for service in 'frontend-apps frontend-bugs cache' ; do
        docker-compose -f ./docker-compose.yml stop ${service}
    done
}

usage () {
    cat <<HELP_USAGE
Starts the Docker Compose environment of Eclipse Steady.

Requires: docker-compose >= $DC_REQUIRED

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

check_docker_version() {
    docker_version=`docker-compose --version | egrep -o "[0-9]+\.[0-9]+\.?[0-9]*"`
    IFS='.' read -ra iv <<< "$docker_version"
    IFS='.' read -ra rv <<< "$DC_REQUIRED"
    if [[ "$(( ${iv[0]} ))" < "$(( ${rv[0]} ))" || ( "$(( ${iv[0]} ))" == "$(( ${rv[0]} ))" &&  "$(( ${iv[1]} ))" < "$(( ${rv[1]} ))" ) ]]; then
        printf "Requirement on Docker-Compose not met (installed: $docker_version, required: $DC_REQUIRED)\n"
        return 0
    else
        return 1
    fi
}

docker_error() {
    printf "Error executing docker-compose\n"
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

# Check requirements
check_docker_version
rc=$?
if [[ $rc == 0 ]]; then
    printf "Script aborted\n"
    exit 1
fi

# Start different sets of services
case $SERVICES in
    none)
        docker-compose -f ./docker-compose.yml stop
        rc=$?
        if [[ $rc == 0 ]]; then
            printf "Stopped all of Steady's Docker Compose services\n"
            more_info
        else
            docker_error
        fi
        ;;
    core)
        stop_ui
        docker-compose -f ./docker-compose.yml up -d --build
        rc=$?
        if [[ $rc == 0 ]]; then
            printf "Started Steady's core Docker Compose services\n"
            core_usage; more_info
        else
            docker_error
        fi
        ;;
    ui)
        docker-compose -f ./docker-compose.yml --profile ui up -d --build
        rc=$?
        if [[ $rc == 0 ]]; then
            printf "Started all of Steady's Docker Compose services\n"
            core_usage; ui_usage; more_info
        else
            docker_error
        fi
        ;;
esac
