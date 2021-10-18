# Quickstart

This section provides the bare minimum to setup Steady and to use its Maven plugin for scanning a Java application.

1. The Steady **backend**, a Docker Compose application, stores information about open-source vulnerabilities and scan results. It has to be installed once, ideally on a dedicated host, and must be running during application scans.

    Download and run [`setup-steady.sh`](https://raw.githubusercontent.com/eclipse/steady/release-@@PROJECT_VERSION@@/docker/setup-steady.sh) to install the backend on any host with a recent version of Docker/Docker Compose (the use of profiles requires a version >= 1.28, installable with `pip install docker-compose` or as [described here](https://github.com/docker/compose#where-to-get-docker-compose)).

    **Notes**: During its first execution, triggered by the setup script or directly using `start-steady.sh -s ui`, the backend will be bootstrapped by downloading and processing code-level information of hundreds of vulnerabilities maintained in the open-source knowledge base [Project KB](https://github.com/sap/project-kb). While the bootstrapping can take up to one hour, later updates will import the delta on a daily basis. Run `start-steady.sh -s none` to shut down all Docker Compose services of the backend.

2. A Steady **scan client**, e.g. the Maven plugin, analyzes the code of your application project and its dependencies. Being [available on Maven Central](https://search.maven.org/search?q=g:org.eclipse.steady), the clients do not require any installation. However, they need to be run whenever your application's code or dependencies change.

    In case application scan and Steady backend run on different hosts, the scan clients must be configured accordingly. Just copy and adjust the file `~/.steady.properties`, which has been created in the user's home directory during the backend setup.

    For Maven, `cd` into your project and run the `app` analysis goal as follows (see [here](https://eclipse.github.io/steady/user/manuals/analysis/) for more information about available goals):

    `mvn org.eclipse.steady:plugin-maven:3.2.0:app`

    **Note**: During application scans, a lot of information about its dependencies is uploaded to the backend, which makes that the first scan takes significantly more time than later scans of the same application.

# Next Steps

In the following a set of step-by-step guides on how to perform the most common operations with @@PROJECT_NAME@@.
For more detailed documentation, please check out the [@@PROJECT_NAME@@ Manual](../manuals/).

## Workspace

If multiple applications or application versions are scanned, it makes sense to create dedicated workspaces rather than keeping all scan results in the default workspace.

* [Create a workspace](./workspace_howto/) (Beginner)
* [Workspaces for multiple releases/branches](./workspace_howto_advanced/) (Advanced)

## Java

* [Scanning Java apps built with Maven](./java_maven/) (Beginner)
* [Scanning Java apps built with Gradle](./java_gradle/) (Beginner)
* [Scanning Java apps (no/other build system)](./java_cli/) (Beginner) - please follow this tutorial if your project neither uses Maven nor Gradle.
* [Reachability analysis (Java/Maven)](./java_maven_advanced/) (Advanced)

## Python

* [Scanning Python apps (other build systems)](./python_cli/) (Beginner)

## Other topics

* [Automating @@PROJECT_NAME@@ scans with Jenkins](./jenkins_howto/)
* [Demystifying @@PROJECT_NAME@@ reports](./reports/)
