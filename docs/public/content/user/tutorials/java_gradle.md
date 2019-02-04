# Scanning Java apps built with Gradle

## Prerequisites

1. JDK 7 or later
2. URL of the package repository to download the plugin JAR (`@@PACKAGE_REPO@@`)
3. URLs of the **backend service** and **apps Web frontend**
    - Apps Web frontend: @@ADDRESS@@/apps
    - Backend service: @@ADDRESS@@/backend/
4. The token of a @@PROJECT_NAME@@ workspace

{! user/tutorials/partials/create_workspace.md !}

## Setup

The plugin for Gradle requires changes of the following two files:

**build.gradle**

```gradle
    buildscript {
        repositories {
            maven { url '@@PACKAGE_REPO@@' }
            mavenCentral()
        }

        dependencies {
            classpath('com.sap.research.security.vulas:plugin-gradle:@@PROJECT_VERSION@@') { changing = true }
        }
    }

    allprojects {
        apply plugin: "vulas"
    }
```

**gradle.properties**

```gradle
    // Used to identify the scan in the apps Web frontend
    group = <GROUP>
    version = <VERSION>

    // Replace token of test space
    vulas.core.space.token = <WORKSPACE-TOKEN>

    vulas.shared.backend.serviceUrl = @@ADDRESS@@/backend/
```

Note: Rather than adding configuration settings to `gradle.properties`, they can also be passed as project properties in the command line, e.g., `-Pvulas.report.exceptionThreshold=noException`. The use of `-D` system properties for changing configuration settings is discouraged (because of the cache of the Gradle daemon).

The configuration is correct if the @@PROJECT_NAME@@ analysis goals `app`, `a2c` etc. are listed among **Other tasks** when running the following command:

```sh
    ./gradlew tasks -all
```

The Gradle plugin only works with later releases of Gradle. How to upgrade is described [here](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:upgrading_wrapper).

## Goal execution

See [here](../../manuals/analysis/) for a description of all analysis goals.

### app

1. `./gradlew assemble vulasApp`

2. Connect to the apps Web frontend, then select your workspace and application. The table in tab [Dependencies](../../manuals/frontend/#dependencies) is populated. Dependencies with known vulnerabilities are shown in tab [Vulnerabilities](../../manuals/frontend/#vulnerabilities).

!!! info "Assess and mitigate"
    Once `app` has been run, the assessment of findings can already start: Each finding shown on the [Vulnerabilities](../../manuals/frontend/#vulnerabilities) tab corresponds to a dependency of an application on a component with a known security vulnerability. See [here](../../manuals/assess_and_mitigate/) for more information on how to assess and mitigate findings. Other analysis goals can be used to collect further evidence concerning the reachability of vulnerable code.

### report

1. `./gradlew vulasReport`

2. A summary report is written to disk (in HTML, XML and JSON format), by default into folder `target/vulas/report`. By default a build exception is thrown if the application includes a library subject to known vulnerabilities.

### clean

1. `./gradlew vulasClean`

2. All application-specific data in the @@PROJECT_NAME@@ backend are deleted.

!!! info "Run clean whenever the application changes"
    If you already scanned your project in the past, you should run the `clean` goal prior to new analyses in order to delete the old analysis results in the backend. Otherwise, old analysis results will be shown together with new results. For example, if you updated a dependency from a vulnerable to a non-vulnerable version, both versions will be shown in the apps Web frontend.

## Useful links

- [Automate](../../tutorials/jenkins_howto) with Jenkins
- [Configure](../../manuals/setup/) the client-side analysis
- [Get help](../../support) if you run into troubles
- [Assess and mitigate](../../manuals/assess_and_mitigate) reported vulnerabilities
