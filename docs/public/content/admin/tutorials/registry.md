# Push Docker images on a registry

## Pre-requisites

- git
- bash
- docker
- docker-compose

## Generate @@PROJECT_NAME@@ images

In order to generate the Docker images to upload to a local registry, you should generate @@PROJECT_NAME@@'s Java archives. This can be done following [Build JAVA archives/Docker images](../build/) tutorial. To briefly summarize you should build and run an image which will populate your local directories with the @@PROJECT_NAME@@ JARs and WARs. In the end of this preliminary step you should have your images locally, this can be tested with the command `docker images | grep vulnerability-assessment-tool`.

```sh
# sample output for `docker images` command
vulnerability-assessment-tool-generator          @@PROJECT_VERSION@@ a829f93eb9aa  22 hours ago 223MB
vulnerability-assessment-tool-patch-lib-analyzer @@PROJECT_VERSION@@ fbe5ec6de811  22 hours ago 103MB
vulnerability-assessment-tool-rest-backend       @@PROJECT_VERSION@@ 277217bc35b2  22 hours ago 136MB
vulnerability-assessment-tool-rest-lib-utils     @@PROJECT_VERSION@@ 53bbb929895d  22 hours ago 127MB
vulnerability-assessment-tool-frontend-bugs      @@PROJECT_VERSION@@ fab5925fe785  22 hours ago 316MB
vulnerability-assessment-tool-frontend-apps      @@PROJECT_VERSION@@ 191ce235c420  22 hours ago 317MB
```

## Push the images to a registry

A script was created to push the images to a local Docker registry running inside your organization. [This script](https://github.com/SAP/vulnerability-assessment-tool/blob/master/docker/push-images.sh) simply tags the images and pushes them towards the registry.

To use the script you will need:

- a registry in your organization (e.g., [goharbor/harbor](https://github.com/goharbor/harbor))
- a username
- @@PROJECT_NAME@@ used version

Invoke the script with the following positional arguments.

```sh
docker login [registry]
bash push-images.sh -r [registry] -u [username] -v [vulnerability-assessment-tool-version]
```

## Pulling the images from a repository

You can use Docker to pull your images from a registry.

```sh
docker pull [registry]/[username]/vulnerability-assessment-tool-rest-backend:[vulnerability-assessment-tool-version]
```
