#!/bin/bash

REL="3.2.4"
DC_REQUIRED="1.28"

usage () {
    cat <<HELP_USAGE
Installs the Docker Compose environment of Eclipse Steady.

Requires: curl, docker-compose >= $DC_REQUIRED

Usage: $0 [options...]

 -d, --dir <dir>        Specifies the installation directory (must not exist or be empty)
                        Default: steady-$REL

 -t, --tag <tag|commit> Tag or commit used for getting source files from https://github.com/eclipse/steady        
                        Default: release-$REL

 -h, --help             Prints this help text
HELP_USAGE
    exit 0
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

setup (){
    DIR=$1

    printf "Installing Eclipse Steady...\n"
    printf "    from https://raw.githubusercontent.com/eclipse/steady/$TAG/docker\n"
    printf "    into `pwd`/$DIR/\n"

    # Services with mounted volumes for configuration and/or data
    conf_services='haproxy postgresql rest-backend cache kb-importer'
    data_services='patch-lib-analyzer postgresql rest-lib-utils cache kb-importer'
    
    # Create directories
    #mkdir -p $DIR/certs
    for s in $conf_services; do
        mkdir -p $DIR/$s/conf
    done
    for s in $data_services; do
        mkdir -p $DIR/$s/data
    done

    # Download all necessary files
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/docker-compose.yml -o ./$DIR/docker-compose.yml
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/.env.sample        -o ./$DIR/.env
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/start-steady.sh    -o ./$DIR/start-steady.sh
    chmod 744 ./$DIR/start-steady.sh

    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/haproxy/conf/haproxy.cfg                 -o ./$DIR/haproxy/conf/haproxy.cfg
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/postgresql/conf/10-vulas-setup.sh        -o ./$DIR/postgresql/conf/10-vulas-setup.sh 
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/cache/conf/nginx.conf                    -o ./$DIR/cache/conf/nginx.conf
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/kb-importer/conf/kaybeeconf.yaml.sample  -o ./$DIR/kb-importer/conf/kaybeeconf.yaml.sample
    curl -s https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/rest-backend/conf/restbackend.properties -o ./$DIR/rest-backend/conf/restbackend.properties

    # Create default configuration in user's home directory
    home_config="$HOME/.steady.properties"
    cat > "$home_config" <<EOL
# URL of the Steady backend to (from) which analysis results are uploaded (downloaded)
# Default: -
vulas.shared.backend.serviceUrl = http://localhost:8033/backend
EOL
    printf "Created default client configuration $home_config"
}

# Read options
while true; do
    case "$1" in
        -d | --dir )  INSTALL_DIR="$2"; shift 2 ;;
        -t | --tag )  TAG="$2"; shift 2 ;;
        -h | --help ) usage; shift 2 ;;
        -- ) shift; break ;;
        * ) break ;;
    esac
done

# Check requirements
check_docker_version
rc=$?
if [[ $rc == 0 ]]; then
    printf "Installation aborted\n"
    exit 1
fi

# Set tag and installation directory
if [[ -z $TAG ]]; then
    TAG="release-$REL"
    if [[ -z $INSTALL_DIR ]]; then
        INSTALL_DIR="steady-$REL"
    fi
else
    if [[ -z $INSTALL_DIR ]]; then
        INSTALL_DIR="steady-$TAG"
    fi
fi

# Install if installation directory does not exist yet or is empty
if [[ ! -d $INSTALL_DIR || -z "$(ls -A $INSTALL_DIR)" ]]; then

    # Check connectivity and tag
    URL="https://raw.githubusercontent.com/eclipse/steady/$TAG/docker/start-steady.sh"
    RC=`curl -o /dev/null -s -w "%{http_code}\n" $URL`
    if [[ ! $RC == "200" ]]; then
        printf "Cannot reach $URL\n"
        printf "Check your internet connectivity and/or the tag '$TAG'\n"
        printf "Installation aborted\n"
        exit 1
    fi

    setup $INSTALL_DIR

    printf "Installation completed, Steady can be started using ./$INSTALL_DIR/start-steady.sh\n\n"
    printf "IMPORTANT: Before starting it for the first time, change the default passwords in ./$INSTALL_DIR/.env\n\n"

# If a non-empty directory already exists, check whether it contains docker-compose.yml and start-steady.sh
else
    if [[ -f ./$INSTALL_DIR/start-steady.sh && -f ./$INSTALL_DIR/docker-compose.yml ]]; then
        printf "Installation skipped (the directory `pwd`/$INSTALL_DIR/ already contains necessary files)\n"
    else
        printf "Installation aborted (directory `pwd`/$INSTALL_DIR/ is neither empty nor does it contain necessary files)\n"
        exit 1
    fi
fi

# Delegate execution to start-steady.sh
read -p "Press <a> to start all of Steady's Docker Compose services (or any other key to skip execution): " CONTINUE
if [[ $CONTINUE == 'a' ]]; then
    cd $INSTALL_DIR
    case "$CONTINUE" in
        a ) SERVICES="all" ;;
    esac
    printf "Executing Steady with ./$INSTALL_DIR/start-steady.sh -s $SERVICES\n"
    bash start-steady.sh -s $SERVICES
else
    printf "Execution skipped. Check startup options with ./$INSTALL_DIR/start-steady.sh --help)\n"
fi
