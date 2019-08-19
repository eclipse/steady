# Building Docker images from source

## Pre-requisites

- git
- docker
- docker-compose

## Customization

All the following commands are supposed to be executed from the root folder of the project.

If you want to build images specific to a version you can checkout a stable version of Vulas. Usually the `master` branch holds a `-SNAPSHOT` version.

```sh
git checkout tags/@@PROJECT_VERSION@@
```

Make a copy of the sample configuration:

```sh
cp docker/.env.sample docker/.env
```

Customize the file `docker/.env` to match your needs, make sure you set the version you want to build in VULAS_RELEASE.

> In `docker/.env` you must configure at least `POSTGRES_USER=`, you should also configure the `HAPROXY`'s user and password as well as the credentials to access the bugs' frontend

## Generate JAVA archives

At this point, you are ready to build the JAR/WAR artifacts with the following command:

```sh
docker build --tag vulnerability-assessment-tool-generator:@@PROJECT_VERSION@@ -f docker/Dockerfile .
docker run -it --rm -v ${PWD}/docker:/exporter --env-file ./docker/.env -e mvn_flags=-DskipTests vulnerability-assessment-tool-generator:@@PROJECT_VERSION@@
```

> If the command above fails, add `-DreuseForks=False` flag to `mvn_flags`. As shown in the example below.
>
> ```sh
> docker run -it --rm -v ${PWD}/docker:/exporter --env-file ./docker/.env -e mvn_flags='-DskipTests -DreuseForks=False' vulnerability-assessment-tool-generator:@@PROJECT_VERSION@@
> ```

> In case you are running behind a proxy you need to configure it in the `--build-arg` arguments. Check the [predefined `ARG`s](https://docs.docker.com/engine/reference/builder/#predefined-args) documentation to know more.

As a result, the folders `docker/<component-name>` will contain compiled JARs (or WARs, depending on the component). The folder `docker/client-tools` will be populated with the JARs for client side tools (CLI, plugins, patchanalyzer).

Additionally, you may want to make the artifacts available to the developers of your organization (e.g., through an internal Nexus or other artifact distribution system).

## Generate Docker images

You are now ready to run the system with the generated archives and create the Docker images:

```sh
(cd docker && docker-compose -f docker-compose.build.yml build)
```

You can create and run containers from the generated images.

```sh
(cd docker && docker-compose -f docker-compose.build.yml up -d)
```

To check everything started successfully, browse the page `http://localhost:8033/haproxy?stats`. All endpoints should appear as green.

> `username` and `password` can be found in your `.env` file, be also advised that `rest-backend` could take more than 30 seconds to be ready to answer HTTP requests
