# Scanning Python apps with the CLI

> Using @@PROJECT_NAME@@ for Python applications not built with SetupTools

## Prerequisites

1. JDK 7 or later
2. `pip` is installed and "knows" all application dependencies (check with `pip list`)
3. URLs of the **backend service** and **apps Web frontend**
    - Apps Web frontend: @@ADDRESS@@/apps
    - Backend service: @@ADDRESS@@/backend/
4. The token of a @@PROJECT_NAME@@ workspace

{! user/tutorials/partials/create_workspace.md !}

## Download

Please download the latest ZIP archive `steady-cli-@@PROJECT_VERSION@@.zip` from @@CLI_ZIP_LOCATION@@ and extract it into a newly created folder.
This folder will contain the following items:

**`./app/`**

: Put the application code (python files) into this folder. It will be searched recursively, thus, it is possible to just copy the entire installation directory of an application into the folder.

**`./steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar`**

: An executable JAR, which is the actual command-line version of the @@PROJECT_NAME@@ client. This is what you will use later to execute @@PROJECT_NAME@@ scans.

**`./steady-custom.properties.sample`**

: This is a template for the configuration file required by @@PROJECT_NAME@@. You will change it in order to specify an identifier for your application (see below).

## Setup

1. Rename the file `steady-custom.properties.sample` to `steady-custom.properties` and edit it to specify `<GROUP>`, `<ARTIFACT>` and `<VERSION>` of the application to be analyzed. Those settings will be used to uniquely identify the application in the backend. For Python applications, feel free to use the same value for both `<GROUP>` and `<ARTIFACT>`.
2. Set the option `vulas.core.space.token` so that it is assigned your own workspace token.
3. Put the application code (python files) into the folder `./app/`.
4. Specify the setting `vulas.core.bom.python.pip` such that it points to a `pip` binary (not only the path in which the binary is located, but the binary itself, e.g., `/foor/bar/pip`). `pip` will be used to determine the dependencies of your Python application. You can either use the global `pip` or one installed in a virtual environment (`virtualenv`, Anaconda, etc.).

```ini
# Full path to PIP binary (e.g., global installation, virtual environment or Anaconda)
#vulas.core.bom.python.pip = <PATH-TO-PIP-BINARY>/pip
```

Additional notes:

- Java resources contained in folder `./app/` will also be added as application code or dependencies.

- For the above reason, make sure that the @@PROJECT_NAME@@ CLI is not contained in `./app/` or other folders specified by setting `vulas.core.app.sourceDir`, if any.

## Goal execution

See [here](../../manuals/analysis/) for a description of all analysis goals.

### app

1. `java -jar steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal app`

2. Connect to the apps Web frontend, then select your workspace and application. The table in tab [Dependencies](../../manuals/frontend/#dependencies) is populated. Dependencies with known vulnerabilities are shown in tab [Vulnerabilities](../../manuals/frontend/#vulnerabilities).

!!! info "Assess and mitigate"
    Once `app` has been run, the assessment of findings can already start: Each finding shown on the [Vulnerabilities](../../manuals/frontend/#vulnerabilities) tab corresponds to a dependency of an application on a component with a known security vulnerability. See [here](../../manuals/assess_and_mitigate/) for more information on how to assess and mitigate findings. Other analysis goals can be used to collect further evidence concerning the reachability of vulnerable code.

### report

1. `java -jar steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal report`

2. Check the console to see where the HTML, JSON and XML reports have been written to.

### clean

1. `java -jar steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal clean`

2. All application-specific data in the @@PROJECT_NAME@@ backend are deleted.

!!! info "Run clean whenever the application changes"
    If you already scanned your project in the past, you should run the `clean` goal prior to new analyses in order to delete the old analysis results in the backend. Otherwise, old analysis results will be shown together with new results. For example, if you updated a dependency from a vulnerable to a non-vulnerable version, both versions will be shown in the apps Web frontend.

## Useful links

- [Automate](../../tutorials/jenkins_howto/) with Jenkins
- [Configure](../../manuals/setup/) the client-side analysis
- [Get help](../../support/) if you run into troubles
- [Assess and mitigate](../../manuals/assess_and_mitigate/) reported vulnerabilities
