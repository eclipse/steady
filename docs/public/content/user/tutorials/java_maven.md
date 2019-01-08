# Scanning Java apps built with Maven

<!--

HP: Not needed in my option.

!!! info "Not using Maven?"

    If your Java application is not build with Maven, you can still use @@PROJECT_NAME@@.

    We have a dedicated tutorial showing [how to use @@PROJECT_NAME@@ with non-Maven Java applications](../tutorials/java_cli/),
    check that out!

-->

## Prerequisites

1. JDK 7 or later
2. Apache Maven 3.x with `settings.xml` configured to download from the Maven repository hosting the @@PROJECT_NAME@@ plugin for Maven
3. URLs of the **backend service** and **apps Web frontend**
	- Apps Web frontend: http://@@HOST@@:@@PORT@@/apps
	- Backend service: http://@@HOST@@:@@PORT@@/backend
4. The token of a @@PROJECT_NAME@@ workspace

{! user/tutorials/partials/create_workspace.md !}

## Setup

The plugin for Maven can be used with or without adding a @@PROJECT_NAME@@ profile to the `pom.xml` of your application. This tutorial guides you through the setup ***with @@PROJECT_NAME@@ profile***.

!!! info "Cannot add the @@PROJECT_NAME@@ profile?"
	If possible we recommend to use the @@PROJECT_NAME@@ profile as the execution of goals using the command-line is more concise (readable). If you cannot paste the @@PROJECT_NAME@@ profile in the `pom.xml` of your application you can still use the @@PROJECT_NAME@@ plugin for Maven. Check out the Manual [Java (Maven)](../../manuals/setup#maven)

Add the following to the `<profiles>` section of the `pom.xml` of your application project and make sure that `<vulas.shared.backend.serviceUrl>` points to the URL of the backend service and that `<vulas.core.space.token>` specifies the token of your individual workspace (see highlighted lines).

{! user/tutorials/partials/vulas_profile.md !}

In case of aggregated, multi-module Maven projects with modules inheriting from their parent, it is sufficient to include the profile in the top-level (parent) `pom.xml`. If a module does not inherit from the parent, the profile has to be added to its POM file.

## Goal execution

See [here](../../manuals/analysis/) for a description of all analysis goals.

### app

1. `mvn -Dvulas compile vulas:app`

2. Connect to the apps Web frontend, then select your workspace and application. The table in tab [Dependencies](../../manuals/frontend/#dependencies) is populated. Dependencies with known vulnerabilities are shown in tab [Vulnerabilities](../../manuals/frontend/#vulnerabilities).

In case your application does not follow the standard structure of Maven applications, please refer to the Manual [Java (Maven)](../../manuals/setup/#maven) to learn how to configure the @@PROJECT_NAME@@ plugin accordingly.

!!! info "Assess and mitigate"
	Once `app` has been run, the assessment of findings can already start: Each finding shown on the [Vulnerabilities](../../manuals/frontend/#vulnerabilities) tab corresponds to a dependency of an application on a component with a known security vulnerability. See [here](../../manuals/assess_and_mitigate/) for more information on how to assess and mitigate findings. Other analysis goals can be used to collect further evidence concerning the reachability of vulnerable code.

### report

1. `mvn -Dvulas vulas:report`

2. A summary report is written to disk (in HTML, XML and JSON format), by default into folder `target/vulas/report`. By default a build exception is thrown if the application includes a library subject to known vulnerabilities.

!!! warning
	The goal `report` must be executed separately from the other analysis goal. Otherwise, in case of multi-module Maven projects, it may throw a build exception before all modules have been analyzed.

### clean

1. `mvn -Dvulas vulas:clean`

2. All application-specific data in the @@PROJECT_NAME@@ backend are deleted.

!!! info "Run clean whenever the application changes"
	If you already scanned your project in the past, you should run the `clean` goal prior to new analyses in order to delete the old analysis results in the backend. Otherwise, old analysis results will be shown together with new results. For example, if you updated a dependency from a vulnerable to a non-vulnerable version, both versions will be shown in the apps Web frontend.

## Useful links

- [Automate](../../tutorials/jenkins_howto) with Jenkins
- [Configure](../../manuals/setup/) the client-side analysis
- [Get help](../../support) if you run into troubles
- [Assess and mitigate](../../manuals/assess_and_mitigate) reported vulnerabilities
