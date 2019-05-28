# Deploy on Docker

In this tutorial you will be guided through the necessary steps to set-up the @@PROJECT_NAME@@ backend services.

<center class='expandable'>
    [![start_page](../img/components.png)](../img/components.png)
</center>

!!! warning "Important Remark"

    The setup obtained following these instructions is meant for demonstration purposes.
    It **shall not** be used in productive scenarios (both for security and scalability concerns).
    <!--Detailed instructions on how to operate @@PROJECT_NAME@@ in a productive environment are given in the [Admin Guide](admin_guide.md).-->

## Pre-requisites

- git
- docker

## Installation

### Clone from GitHub

```sh
git clone https://github.com/SAP/vulnerability-assessment-tool
```

### Build Docker images

All the following commands are supposed to be executed from the root folder of the project.
Before proceeding, be sure to move there with:

```sh
cd vulnerability-assessment-tool
```

Make a copy of the sample configuration:

```sh
cp docker/.env.sample docker/.env
```

Customize the file `docker/.env` to match your needs.

!!! info "Sensitive information"

	In `docker/.env` you must configure at least `POSTGRES_USER=`, you should also configure the `HAPROXY`'s user and password as well as the credentials to access the bugs' frontend

At this point, you are ready to perform the actual build with the following command:

```sh
docker build --tag vulas-build-img -f docker/Dockerfile --build-arg http_proxy= --build-arg https_proxy= .
docker run -it --rm -v ${PWD}/docker:/exporter --env-file ./docker/.env -e mvn_flags=-DexcludedGroups=com.sap.psr.vulas.shared.categories.Slow vulas-build-img
```

!!! warning "Build error"

	If the command above fails, add `-DreuseForks=False` flag to `mvn_flags`. As shown in the example below.

    ```sh
    docker run -it --rm -v ${PWD}/docker:/exporter --env-file ./docker/.env -e mvn_flags='-DexcludedGroups=com.sap.psr.vulas.shared.categories.Slow -DreuseForks=False' vulas-build-img
    ```

In case you are running behind a proxy you need to configure it in the `--build-arg` arguments.

As a result, the folders `docker/<component-name>` will contain compiled JARs (or WARs, depending on the component). The folder `docker/client-tools` will be populated with the JARs for client side tools (CLI, plugins, patchanalyzer).

Finally, you may want to make all artifacts available to the developers of your organization (e.g., through an internal Nexus or other artifact distribution system).

### Run

You are now ready to run the system:

```sh
(cd docker && docker-compose up -d --build)
```

To check if everything started successfully, check the page `http://localhost:8033/haproxy?stats`. All endpoints should appear as green (you may want to replace `localhost` with the actual hostname of your machine).

!!! info "Credentials and start up time"
    `username` and `password` can be found in your `.env` file, be also advised that `rest-backend` could take more than 30 seconds to be available to answer HTTP requests

### Populate/maintain the vulnerability database

In order for the tool to detect vulnerabilities, you need to import and analyze them first so that they are available in the tool's vulnerability database. Large part of CVE's and bugs are open sourced in [vulnerability-assessment-kb](https://github.com/SAP/vulnerability-assessment-kb).

Follow the instructions mentioned [here](../../../vuln_db/tutorials/vuln_db_tutorial/#batch-import-from-knowledge-base), to import and build all the vulnerabilities' knowledge.

Get going:

1. [Import](../../../vuln_db/tutorials/vuln_db_tutorial) all the CVEs and bugs in your local datababse
2. Setup your [workspace](../../../user/manuals/setup/#workspace) (if you don't have one)
3. Become familiar with the various analysis [goals](../../../user/manuals/analysis/) (first time users)
4. Analyze your [Java](../../../user/tutorials/java_maven) or [Python](../../../user/tutorials/python_cli) application (on a regular basis)
5. [Assess](../../../user/manuals/assess_and_mitigate) findings using the apps Web frontend (following every analysis)

Further links:

- [Configure](../../../user/tutorials/) the client-side analysis
- [Automate](../../../user/tutorials/jenkins_howto) with Jenkins
- [Get help](../../../user/support) if you run into troubles
