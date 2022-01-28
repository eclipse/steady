# Scanning Java apps with the CLI

This tutorial will walk you through the steps needed to setup and use @@PROJECT_NAME@@ to scan
a Java application that is **not built with tools such as Maven or Gradle**.

!!! info "Terminology: 'Goals'"
    For consistency with the terminology used in Maven, the different "commands" available in @@PROJECT_NAME@@
    are referred to as "**goals**".

## Prerequisites

1. JDK 7 or later
2. URLs of the **backend service** and **apps Web frontend**
    - Apps Web frontend: @@ADDRESS@@/apps
    - Backend service: @@ADDRESS@@/backend/
3. The token of a @@PROJECT_NAME@@ workspace

{! user/tutorials/partials/create_workspace.md !}

## Download

Please download the latest ZIP archive `steady-cli-@@PROJECT_VERSION@@.zip` from @@CLI_ZIP_LOCATION@@ and extract it into a newly created folder.
This folder will contain the following items:

**`./app/`**

: Put the application code (java, class or JAR files) and all application dependencies (JAR files) into this folder. It will be searched recursively, thus, it is possible to just copy the entire installation directory of an application into the folder.

**`./steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar`**

: An executable JAR, which is the actual command-line version of the @@PROJECT_NAME@@ client. This is what you will use later to execute @@PROJECT_NAME@@ scans.

**`./instr/lang-java-@@PROJECT_VERSION@@-jar-with-dependencies.jar`**

: This is used to instrument the Java runtime when performing dynamic analysis.

**`./steady-custom.properties.sample`**

: This is a template for the configuration file required by @@PROJECT_NAME@@. You will change it in order to specify an identifier for your application (see below).

## Setup

1. Rename the file `steady-custom.properties.sample` to `steady-custom.properties` and edit it to specify `<GROUP>`, `<ARTIFACT>` and `<VERSION>` of the application to be analyzed. Those settings will be used to uniquely identify the application in the backend.
2. Set the option `vulas.core.space.token` so that it is assigned your own workspace token.
3. Put the application code (java, class or JAR files) and all application dependencies (JAR files) into this folder.
4. Specify how @@PROJECT_NAME@@ can distinguish the code of your application from its dependencies, which is necessary for the call graph construction during the reachability analyses (goals `a2c` and `t2c`).

You can do so in two different ways: you can use either `vulas.core.app.appPrefixes` or `vulas.core.app.appJarNames`.

```ini
# Package prefix(es) of application code (multiple values to be separated by comma), only relevant for CLI
vulas.core.app.appPrefixes =

# Regex that identifies JARs with application code (multiple values to be separated by comma), only relevant for CLI
vulas.core.app.appJarNames =
```

!!! warning

    In order for the static reachability analysis to be performed correctly, all application methods must be used as entry points for the call graph construction. Therefore, if the criterion to distinguish application code and dependencies is not specified correctly, the potential execution of vulnerable open-source methods may be missed.

    To check whether the specification is correct, you may want to inspect the application frontend and see whether there are any items in the [Dependencies](../../manuals/frontend/#dependencies) tab that are created by you or your organization (there should be none, only 3rd party libraries should be there), and whether there are open-source packages at all in the table on the [Dependencies](../../manuals/frontend/#application-statistics) tab (there should be none, only packages from your own project).

    **IMPORTANT REMARKS**

    You should also keep the following into account:

    1. Single java and class files are always considered as application code (_regardless_ of the package prefix configured with `vulas.core.app.appPrefixes`).
    2. JARs are always considered application dependencies unless they only contain methods starting with the configured package prefix.
    3. Nested JARs must be manually extracted (but no need to do so for WARs).

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
