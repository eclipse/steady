Java applications can be analyzed either using the Vulas plugins for Maven and Gradle, or using the Vulas CLI. The remainder of this section describes the use of the plugins, the CLI is described [here](CLI.md).

* [I - Prerequisites](#i---prerequisites)
* [II - Setup](#ii---setup)
    * [1 - Maven](#1---maven)
    * [2 - Gradle](#2---gradle)
* [III - Goal Execution](#iii---goal-execution)
    * [1 - app](#1---app)
    * [2 - A2C](#1---a2c)
    * [3 - JUnit Tests](#3---junit-tests)
    * [4 - Integration Tests](#4---integration-tests)
    * [5 - Instrumention Tests (INSTR)](#5---instrumentation-tests)
    * [6 - T2C](#6---t2c)
    * [7 - upload](#7---upload)
    * [8 - report](#8---report)
    * [9 - Clean](#9---clean)
    * [10 - CleanSpace](#10---cleanspace)


## I - Prerequisites

- [Workspace](Workspace.md): Create one and remember its token.
- Java: Make sure to have Java 7 or later installed. **Important**: The static analysis (goals _a2c_ and _t2c_) does not work with Java 9 yet, because the underlying analysis framework does not support it.
- Maven: `settings.xml` is configured to download (the Vulas plugin for Maven) from Nexus' internal repository

## II - Setup

### 1 - Maven

The plugin for Maven can be used with or without adding a Vulas profile to the `pom.xml`.

- **With Vulas profile**, the execution of goals is more concise (readable), e.g., `mvn -Dvulas compile vulas:app`. A sample profile for Vulas can be found [here](Sample-Maven-profile.md), just copy&paste it into the `<profiles>` section of your `pom.xml`.
    - In case of aggregated, multi-module Maven projects with modules inheriting from their parent, it is sufficient to include the profile in the top-level (parent) `pom.xml`. If a module does not inherit from the parent, the profile has to be added to its POM file.
    - Also note that he profile contains a configuration for the maven-surefire-plugin. If you use this module already with specific settings in your default profile, you need to add those settings, e.g., the `<argLine>`, also to its configuration in the Vulas profile.

- **Without Vulas profile**, the execution of goals requires the use of the plugin's fully qualified name, e.g., `mvn compile org.eclipse.steady:plugin-maven:<latest>:app`. Moreover, the [analysis goal](Goals.md) _test_ cannot be executed (because mandatory Vulas settings cannot be passed to the maven-surefire-plugin). Additionally, you need to specify the following mandatory configuration settings. As described [here](Configuration.md), there are several ways of doing so, however, a file as follows is very common:

**vulas-custom.properties**

```ini
    vulas.core.space.token = <YOUR WORKSPACE TOKEN>
    vulas.shared.backend.serviceUrl = http://<hostname>:8033/backend
    
```

Last, it is possible to include/exclude modules of a multi-module Maven project using the following configuration parameters:

```ini
# Options to include and exclude Maven artifacts (modules) during the processing of an aggregator project.
# If includes is provided, the other parameters are ignored. In other words, excludes and ignorePoms will
# only be evaluated if includes is empty.
#
# Important: Those options are ignored in case of the report goal, thus, report will be run on all modules.
# In particular, running report on a module with packaging POM will create an aggregated report for all its
# submodules.
#
# Multiple values for includes and excludes must be separated by comma.
#
# Defaults:
#   includes = -
#   excludes = -
#   ignorePoms = false
vulas.maven.includes =
vulas.maven.excludes =  
vulas.maven.ignorePoms = false
```

### 2 - Gradle

Will be soon available!

## III - Goal execution

### 1 - APP

**Objective:** Create a complete bill of material (BOM) of the application and of all its dependencies (direct and transitive). Most importantly, the BOM comprises the signatures of all Java methods of the application and all dependencies, which is compared with a list of methods known to be vulnerable. Moreover, the BOM also comprises meta-info on archive level, e.g., the manifest file entries or the archive's SHA1.

**Important:** By default, the Vulas Maven plugin searches for application source and compiled code in the folders "src/main/java" and "target/classes". If  source or byte code is generated or modified during the build process, and stored in other folders than the ones mentioned, you need to add those directories using the parameter "vulas.core.app.sourceDir". Otherwise, the respective code will not be used when performing the reachability analysis.

**Example:** Suppose source code is generated into the folder "target/generated-sources". If this code is compiled into the folder "target/classes", you do not need to do anything. If it is compiled into a different folder, you would need to add this folder to entries of "vulas.core.app.sourceDir".

#### Maven

```sh
mvn -Dvulas compile vulas:app
```

#### CLI

```sh
java -jar vulas-cli-<version>-jar-with-dependencies.jar -goal app
```

**Result:** In the Vulas frontend, the table in tab "Dependencies" is populated. In case any of the dependencies has vulnerabilities, they are shown in tab "Vulnerabilities". The column "Inclusion of vulnerable code" indicates whether the version in use is known to be vulnerable or not (see tooltip for more information).

**How does it work:** Each java, class and JAR file is analyzed and contained method signatures are uploaded to the Vulas backend for later comparison.

**Troubleshooting:**

* The plugin execution takes very long
    * Explanation: Whenever a JAR, identified by its SHA1, is unknown to the Vulas backend, all its method signatures are gathered and uploaded. As a result, the first execution(s) of vulas:app can take some time, as amy yet unknown JARs need to be covered.
* The plugin execution breaks with a 5xx response code received from the backend
    * Explanation: This can happen if multiple processes (on the same computer or remotely) try to upload the same method signature to the backend. In such cases, one can simply restart at a later time to avoid the clash.

***

### 2 - A2C

**Objective:** Understand whether vulnerable methods can be potentially reached from application methods.

#### Maven

```sh
mvn -Dvulas compile vulas:a2c
```

#### CLI

```sh
java -jar vulas-cli-<version>-jar-with-dependencies.jar -goal a2c
```

**Result:** In the Vulas frontend, tab "Vulnerabilties", the column "Static Analysis" is populated for all libraries subject to known vulnerabilities. By selecting single row of this table and the one of the detailed page, one can get more information up until the paths of potential executions (if any).

**How does it work:** Vulas uses Wala, a static analysis tool for Java, in order to construct a callgraph representing all possible program executions starting from application methods. This graph is traversed in order to see whether and from where methods with known vulnerabilities can be reached.

**Troubleshooting:**

* The plugin execution terminates with a log message as follows: "timeout reached"
    * Solution 1: Increase the default timeout of 120 min by changing the system property "vulas.reach.timeout" (Maven) or the CLI option "timeout" (CLI), e.g., "mvn -Dvulas.reach.timeout=600 -Dvulas compile vulas:a2c"
    * Solution 2: Decrease the precision of the callgraph construction, e.g., by changing the system property "vulas.reach.wala.callgraph.reflection", e.g., "mvn -Dvulas -Dvulas.reach.wala.callgraph.reflection=NO_FLOW_TO_CASTS_NO_METHOD_INVOKE compile vulas:a2c"
* java.lang.OutOfMemoryError
    * CLI: Increase JVM heap space by adding "-Xmx4096M -Xms2024M" (or more if possible) when performing the CLI call, e.g., "java -Xmx4096M -Xms2024M -jar ..."
    * Maven: Increase the JVM heap space by using the following export statement: export MAVEN_OPTS="-Xmx4096M -Xms1024M -XX:MaxPermSize=2024M -XX:+CMSClassUnloadingEnabled"

***

### 3 - JUnit Tests

**JUnit Tests is a goal dedicated to Maven only!**

**Objective:** Collect method traces during the actual execution of JUnit tests. Information about traced methods will be compared with methods subject to known vulnerabilities.

#### Maven

```sh
mvn -Dvulas test vulas:upload
```

**Result:**

* In the Vulas frontend, tab "Vulnerabilties", the column "Dynamic Analysis" is populated for all libraries subject to known vulnerabilities. By selecting single row of this table and the one of the detailed page, one can get more information up until the paths of actual executions (if any).
* In the Vulas frontend, tab "Test coverage", the number of traced vs. the number of total methods is shown per Java package of the application.

**How does it work:** By registering a Java agent at JVM startup, Vulas changes the bytecode of every Java class loaded at runtime. Vulas adds statements in order to save the timestamp of every method invocation as well as stack trace information. Per default, this information is saved in folder "target/vulas/upload" and uploaded using the goal vulas:upload.

**Troubleshooting:**

* Tests terminate with a log message as follows: "The forked VM terminated without properly saying goodbye. VM crash or System.exit called?"
    * Solution 1: Increase JVM heap space by adding "-Xmx4096M -Xms2048M" (or more if possible) to the <argLine> argument of the Maven Surefire plugin
    * Solution 2: Select an instrumentor that consumes less memory by adding "-Dvulas.core.instr.instrumentorsChoosen=com.sap.psr.vulas.monitor.trace.SingleTraceInstrumentor" to the <argLine> argument of the Maven Surefire plugin
* Tests terminate with the error message "App context incomplete: [group=, artifact=, version=]" and there exists a white space in any of the parent directories of the Maven project (e.g., "C:\My Documents\projects\foo").
    * Solution 1: Move the Maven project to a location without whitespaces in the names of any of the parent directories.
    * Solution 2: Open the POM file and replace the Maven variable ${project.build.directory} in the <argLine> configuration setting of the maven-surefire-plugin by the relative path of the respective directory, typically "target".
    * Solution 3: Open the POM file and remove the -Dvulas.core.uploadDir and -Dvulas.shared.tmpDir system properties in the <argLine> configuration setting of the maven-surefire-plugin. Create two new system property variables in the configuration section of the maven-surefire-plugin as follows (see here for more information).

```xml
<systemPropertyVariables>
  <vulas.core.uploadDir>${project.build.directory}/vulas/upload</vulas.core.uploadDir>
  <vulas.shared.tmpDir>${project.build.directory}/vulas/tmp</vulas.shared.tmpDir>
</systemPropertyVariables>
```

***

### 4 - Integration Tests

**Objective:** Collect method traces during the actual execution of the application. Information about traced methods will be compared with methods subject to known vulnerabilities.

#### Maven and CLI

* Download the file `lang-java-<version>-jar-with-dependencies.jar` to your computer (CLI users can take it from the folder `./instr`), it will be further referenced as <INSTR-JAR>.
* Add the following arguments to the Java runtime (and replace line breaks by a single space characters).

```
-javaagent:<INSTR-JAR>
-Dvulas.shared.backend.serviceUrl=http://<hostname>:8033/backend
-Dvulas.core.backendConnection=READ_WRITE
-Dvulas.core.monitor.periodicUpload.enabled=true
-Dvulas.core.appContext.group=<GROUP>
-Dvulas.core.appContext.artifact=<ARTIFACT>
-Dvulas.core.appContext.version=<VERSION>
-Dvulas.core.instr.instrumentorsChoosen=com.sap.psr.vulas.monitor.trace.SingleTraceInstrumentor
-Dvulas.core.space.token=<WORKSSPACE-TOKEN>
-noverify
```

* Open the application and perform some application-specific tests and workflows.

**Result:** Same as in section "JUnit tests"

**How does it work:** By registering a Java agent at JVM startup, Vulas changes the bytecode of every Java class loaded at runtime. Vulas adds statements in order to save the timestamp of every method invocation as well as stack trace information. This information is periodically uploaded to the Vulas backend.

**Example:** In case of Tomcat 8.x, one needs to (1) copy <INSTR-JAR> into the folder "./bin" and (2) specify the variable CATALINA_OPTS as follows in the file "./bin/setenv.bat". Do not forget to specify values for the placeholders <INSTR-JAR>, <GROUP>, <ARTIFACT> and <VERSION>. Note: The use of "setenv.bat" does not work if Tomcat is run as Windows service.

```sh
set "CATALINA_OPTS=-javaagent:<INSTR-JAR> -Dvulas.shared.backend.serviceUrl=http://<hostname>:8033/backend -Dvulas.shared.cia.serviceUrl=http://<hostname>:8033/cia -Dvulas.core.uploadEnabled=true -Dvulas.core.monitor.periodicUpload.enabled=true -Dvulas.core.appContext.group=<GROUP> -Dvulas.core.appContext.artifact=<ARTIFACT> -Dvulas.core.appContext.version=<VERSION> -Dvulas.core.instr.instrumentorsChoosen=com.sap.psr.vulas.monitor.trace.SingleTraceInstrumentor -noverify"
```

***

### 5 - Instrumentation Tests

**INSTR is a goal dedicated to Maven only!**

**Objective:** Modify an existing JAR (WAR) created by "mvn package" so that traces will be collected once the JAR is executed (the WAR is deployed in a Web application container such as Tomcat). Note: In contrast to what is described in the previous section "Integration Tests", vulas:instr will not result in the collection of traces for Tomcat itself.

**Prerequisite:** An application's JAR or WAR, e.g., as created with "mvn package" in folder "target".

#### Maven

```sh
mvn package
mvn -Dvulas initialize vulas:instr
```

**Result:** A new JAR/WAR with suffix "-vulas-instr" will be created in folder "target/vulas/target".

**How does it work:** The bytecode of all the Java classes found in the JAR (WAR) will be modified as to collect information about, for instance, method execution and stack traces. This information will be uploaded to the Vulas2 backend. Note: The modified code in the new JAR with suffix "-vulas-instr" can be inspected with decompilers such as [JD-GUI](http://jd.benow.ca/).

**Troubleshooting:**

* The console shows compilation errors, e.g., "cannot find javax.servlet.http.HttpServletRequest"
    * The reason is that all application dependencies are re-compiled, and it can happen that some of the classes do have dependency requirements not met by the application. This can be overcome by identifying the respective JAR file and downloading it to the folder "target/vulas/lib".


***

### 6 - T2C

**Objective:** Understand whether vulnerable methods can be potentially reached from traced methods.

**Prerequisite:** Traces must have been collected during JUnit or integration tests (see above)

#### Maven

```sh
mvn -Dvulas compile vulas:t2c
```

#### CLI

```sh
java -Dvulas.core.appContext.group=<GROUP> -Dvulas.core.appContext.artifact=<ARTIFACT> -Dvulas.core.appContext.version=<VERSION>
     -Dvulas.core.app.sourceDir=<APP-FOLDER> -Dvulas.core.app.depDirs=<DEP-FOLDER>
     -Dvulas.reach.timeout=15 -Dvulas.reach.preprocessDependencies=true
     -jar vulas-cli-jar-with-dependencies.jar -goal t2c
```

**Result:** In the Vulas frontend, tab "Vulnerabilties", the column "Static Analysis" is populated for all libraries subject to known vulnerabilities. By selecting single row of this table and the one of the detailed page, one can get more information up until the paths of potential executions (if any).

**How does it work:** In contrast to the goal "vulas:a2c", the callgraph is built starting from all methods that were previously traced. As such, the callgraph construction overcomes weaknesses of static source analysis related to the use of reflection and control inversion. What remains the same is that the resulting graph is traversed in order to see whether and from where methods with known vulnerabilities can be reached.

**Troubleshooting:**

* The plugin execution terminates with a log message as follows: "timeout reached"
    * Solution 1: Increase the default timeout of 120 min by changing the system property "vulas.reach.timeout", e.g., "mvn -Dvulas.reach.timeout=600 -Dvulas compile vulas:t2c"
    * Solution 2: Decrease the precision of the callgraph construction, e.g., by changing the system property "vulas.reach.wala.callgraph.reflection", e.g., "mvn -Dvulas -Dvulas.reach.wala.callgraph.reflection=NO_FLOW_TO_CASTS_NO_METHOD_INVOKE compile vulas:t2c"
* java.lang.OutOfMemoryError
    * CLI: Increase JVM heap space by adding "-Xmx4096M -Xms2024M" (or more if possible) when performing the CLI call, e.g., "java -Xmx4096M -Xms2024M -jar ..."
    * Maven: Increase the JVM heap space by using the following export statement: export MAVEN_OPTS="-Xmx4096M -Xms1024M -XX:MaxPermSize=2024M -XX:+CMSClassUnloadingEnabled"

***

### 7 - UPLOAD

Except for execution traces, data is uploaded right away to the backend, due to the default setting of the parameter `vulas.core.backendConnection`. If this setting is `READ_ONLY`, all data is written to folder `vulas.core.uploadDir`. Such data can later be uploaded to the backend using the _upload_ goal.

The _upload_ goal has the following configuration options:

```ini
    # When true, serialized HTTP requests will be deleted after the upload succeeded (incl. the JSON files)
    # Default: true
    vulas.core.upload.deleteAfterSuccess = true
```

*** 

### 8 - REPORT

**Objective:** Creates result reports in HTML, XML and JSON format (on the basis of analysis results downloaded from the Vulas backend). Additionally, the Maven and Gradle plugins can be configured to throw a build exception in order break Jenkins jobs and pipelines in case vulnerable code is present (or reachable/executed). The HTML report can be copied into a Jenkins dashboard using the [HTML Publisher Plugin](http://wiki.jenkins-ci.org/display/JENKINS/HTML+Publisher+Plugin) (see [automation](Automation.md) for a configuration example).

**Multi-module Maven projects**: The report goal should be called in a separate build step, e.g., `mvn -Dvulas vulas:report`. It must NOT be called together with other Vulas goals, e.g., `mvn -Dvulas compile vulas:app vulas:report`, because the build may fail before _app_ and other goals are executed for all the modules. Alternatively, you can use the Maven option `--fail-at-end` (see [here](https://maven.apache.org/guides/mini/guide-multiple-modules.html) for more info).

The following configuration settings decide whether and when build exceptions are thrown. They can be used to capture the results of an assessment by developers whether a vulnerability is problematic in a given application context. Excluded scopes and bugs are also shown in the apps Web frontend.

```ini
    # A vulnerability in blacklisted scopes will not cause an exception  (multiple scopes to be separated by comma)
    # Default: test, provided
    # Note: For CLI, all dependencies are considered as RUNTIME dependencies
    vulas.report.exceptionScopeBlacklist = TEST, PROVIDED
    
    # Specified vulnerabilities will not cause a build exception (multiple bugs to be separated by comma)
    # Default: -
    vulas.report.exceptionExcludeBugs = <vuln-id>
    
    # Explanation why the given vulnerability is not relevant/exploitable in the specific application context
    # Default: -
    vulas.report.exceptionExcludeBugs.<vuln-id> = Not exploitable because ...

    # Determines whether un-assessed vulnerabilities throw a build exception. Un-assessed vulns are those where
    # the method signature(s) of a bug appear in an archive, however, it is yet unclear whether the methods
    # exist in the fixed or vulnerable version. Those findings are marked with a question mark in the frontend.
    #
    # Possible values:
    #   all: All un-assessed vulns will be ignored
    #   known: Only un-assessed vulns in archives known to Maven Central will be ignored
    #   off: Never ignore
    #
    # Default: all
    vulas.report.exceptionExcludeUnassessed = all
    
    # Directory to where the reports (JSON, XML, HTML) will be written to
    # Default:
    #   CLI: -
    #   MVN: ${project.build.directory}/vulas/report
    vulas.report.reportDir =
```

#### Maven

```sh
mvn -Dvulas vulas:report
```

#### CLI

```sh
java -Dvulas.core.appContext.group=<GROUP> -Dvulas.core.appContext.artifact=<ARTIFACT> -Dvulas.core.appContext.version=<VERSION>
     -jar vulas-cli-jar-with-dependencies.jar -goal report
```

**Result:** A summary report is written to disk (in HTML, XML and JSON format). For Maven, the target directory of the different files is "target/vulas/report", for Gradle it is "build/vulas/report". For CLI, the exact location is printed to the console.

**How does it work:** Identified vulnerabilities including any information gathered during static and dynamic analysis will be downloaded from the Vulas backend.

**Troubleshooting:**

* The Maven plugin supports several configuration settings in order to fine-tune the threshold that breaks a build process. These settings can be specified in the plugin configuration, through Java -D system properties or in a properties file.
    * vulas.report.exceptionExcludeBugs can be used to exclude certain bugs (default: none)
    * vulas.report.exceptionScopeBlacklist can be used to exclude certain Maven scopes (default: test)
    * vulas.report.exceptionThreshold can be used to specify whether a build exception is thrown when vulnerable code is included, potentially reachable, actually reached or not at all (values: noException, dependsOn, potentiallyExecutes, actuallyExecutes; default: actuallyExecutes)


***

### 9 - clean

Deletes application-specific data in the backend, e.g., traces collected during JUnit tests, or application constructs and dependencies collected through the _app_ goal. Right after executing _clean_ for a given application, the apps Web frontend will be empty for the respective application.

The following configuration settings determine the behavior of the clean goal. You can use `vulas.core.clean.purgeVersions` to delete previous versions of the application. In combination with `vulas.core.clean.purgeVersions.keepLast`, it can be used to delete ALL versions.

```ini
    # When true, details of past goal executions will be deleted
    # Default: false
    vulas.core.clean.goalHistory = false
     
    # When true, all but the latest X app versions will be deleted (latest according to the application creation date)
    # Default: false
    vulas.core.clean.purgeVersions = false
     
    # Specifies X, i.e., the number of application versions to be kept if purgeVersions is set to true (0 will delete all versions)
    # Default: 3
    vulas.core.clean.purgeVersions.keepLast = 3
```

***

### 10 - cleanSpace

Deletes all applications of the given space.

**Important**: The default workspace (public) cannot be cleaned.
