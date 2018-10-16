# Trying out Vulas (with Docker)

**IMPORTANT REMARK**

The setup obtained following these instructions is meant for demonstration purposes.
It **shall not** be used in productive scenarios (both for security and scalability concerns).

## Pre-requisites

- docker
- git

## Installation

### Clone the Vulas repository from GitHub
```
git clone https://github.com/SAP/vulnerability-assessment-tool
```

### Build Vulas

All the following commands are supposed to be executed from the root folder of the project.
Before proceeding, be sure to move to move there with:

```
cd vulnerability-assessment-tool
```

Make a copy of the sample configuration:

```
cp docker/.env.sample docker/.env
```

Edit the file `docker/.env` to match your needs.
At this point, you are ready to perform the actual build with the following command:

```
docker build --tag vulas-build-img -f docker/Dockerfile --build-arg http_proxy= --build-arg https_proxy=  . 
docker run -it --rm -v ${PWD}:/vulas --env-file ./docker/.env vulas-build-img
```

In case you are running behind a proxy you need to configure it in the `--build-arg` arguments.

As a result, the folders `<component-name>/target` will contain compiled JARs (or WARs, depending on the component).
You need to copy the following ones into the corresponding folders inside the `docker/` folder.

* `frontend-apps-<version>.war`
* `frontend-bugs-<version>.war`
* `rest-backend-<version>.jar`
* `rest-lib-utils-<version>.jar`
* `patch-lib-analyzer-<version>-jar-with-dependencies.jar`

For example, assuming you are in the top-level folder of the source tree, you can use a command like
`cp frontend-apps/target/frontend-apps-3.0.10-SNAPSHOT.war docker/frontend-apps/`

Finally, you may want to make all artifacts available to the developers of your organization (e.g., through an internal Nexus or other artifact distribution system).

### Run

You are now ready to run the system:

```
(cd docker && docker-compose up -d --build)
```

To check that everything started successfully, check the page `http://localhost:8033/haproxy?stats` (user/pwd: `admin/admin`).
All endpoints should appear as green (you may want to replace `localhost` with the actual hostname of your machine).

### Populate/maintain the vulnerability database

In order for the tool to detect vulnerabilities, you need to analyze them first so that they are available in
the tool's vulnerability database.

To do so, please follow the instructions mentioned [here](readme-pages/Vulnerability-Database.md).

### Analyze an application

**Pre-requisite**: Please, make sure you take note of the URL of the **backend service** as well as the URL of the **apps Web frontend**, you will need these:

- Apps Web frontend: [http://localhost:8033/apps](https://localhost:8033/apps)
- Backend service: [http://localhost:8033/backend](http://localhost:8033/backend)

You may want to replace `localhost` with the actual hostname of your machine.

Get going:
1. Setup your [workspace](readme-pages/Workspace.md) (if you don't have one)
2. Become familiar with the various analysis [goals](readme-pages/Goals.md) (first time users)
3. Analyze your [Java](readme-pages/Java.md) or [Python](readme-pages/Python.md) application (on a regular basis)
4. [Check the Vulas setup](readme-pages/Configuration.md) (once, after the initial setup and first analysis) 
5. [Assess](readme-pages/Assessment-and-Mitigation.md) findings using the apps Web frontend (following every analysis)

Further links:
- [Automate](readme-pages/Automation.md) with Jenkins
- [Configure](readme-pages/Configuration.md) the client-side analysis
- [Get help](readme-pages/Help.md) if you run into troubles




