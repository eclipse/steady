# Scanning Java apps built with Maven

!!! warning "Beginner? Read here"
    This tutorial is the second part of the [introductory tutorial that you can find here](../java_maven/). If you have not done so yet, you are warmly encouraged to follow that one first, and then come back here. This document will assume you already have a working configuration and have successfully executed the steps described in the basic tutorial.

## Prerequisites

The same prerequisites as in the [introductory tutorial](../java_maven/) also apply here.

## Goal execution

The introductory tutorial explained how to use the `app` analysis goal in order to establish whether an application projects depends on open-source libraries that **contain vulnerable code**.

This tutorial explains how to perform the static and dynamic analyses in order to collect evidence regarding the reachability or **execution of vulnerable code** in the context of a given application project. The reachability of vulnerable code is an important pre-requisite for the vulnerability to be exploitable.

### a2c

1. `mvn -Dvulas compile vulas:a2c`

2. Connect to the apps Web frontend, then select your workspace and application. In the tab [Vulnerabilities](../../manuals/frontend/#vulnerabilities), the column **Static Analysis** is populated for all libraries subject to known vulnerabilities reachable from application code. By selecting a single row of this table and the one of the detailed page, one can get more information up until the paths of potential executions (if any).

### test

1. `mvn -Dvulas vulas:prepare-vulas-agent test vulas:upload`

2. Connect to the apps Web frontend, then select your workspace and application.

- In the tab [Vulnerabilities](../../manuals/frontend/#vulnerabilities), the column **Dynamic Analysis** is populated for all libraries subject to known vulnerabilities whose vulnerable code is executed during tests. By selecting single row of this table and the one of the detailed page, one can get more information up until the paths of actual executions (if any).
- In the tab [Statistics](../../manuals/frontend/#application-statistics), the number of traced vs. the number of total executable constructs (e.g., Java methods) is shown per application package.

### t2c

1. `mvn -Dvulas compile vulas:t2c`

2. Connect to the apps Web frontend, then select your workspace and application. In the tab [Vulnerabilities](../../manuals/frontend/#vulnerabilities), the column **Static Analysis** is populated for all libraries subject to known vulnerabilities reachable from code executed during tests. By selecting single row of this table and the one of the detailed page, one can get more information up until the paths of potential executions (if any).

### report

1. `mvn -Dvulas vulas:report`

2. A summary report is written to disk (in HTML, XML and JSON format), by default into folder `target/vulas/report`. By default a build exception is thrown if the application includes a library subject to known vulnerabilities.

!!! warning
    The goal `report` must be executed separately from the other analysis goal. Otherwise, in case of multi-module Maven projects, it may throw a build exception before all modules have been analyzed.

### clean

1. `mvn -Dvulas vulas:clean`

2. All application-specific data in the @@PROJECT_NAME@@ backend are deleted.

!!! info "Run clean whenever the application changes"
    If you already scanned your project in the past, you should run the `vulas:clean` goal prior to new analyses in order to delete the old analysis results in the backend. Otherwise, old analysis results will be shown together with new results. For example, if you updated a dependency from a vulnerable to a non-vulnerable version, both versions will be shown in the apps Web frontend.

## Useful links

- [Automate](../../tutorials/jenkins_howto/) with Jenkins
- [Configure](../../manuals/setup/) the client-side analysis
- [Get help](../../support/) if you run into troubles
- [Assess and mitigate](../../manuals/assess_and_mitigate/) reported vulnerabilities
