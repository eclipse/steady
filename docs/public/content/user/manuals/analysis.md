# Analysis Manual

## Overview

The various client-side tools offer so-called **goals** in order to analyze applications and interact with the backend.

The following goals perform some sort of application analysis:

- `app`: Creates a method-level bill of material of an application and all its dependencies.
- `a2c`: Builds a call graph (starting from app methods) and checks whether vulnerable code is potentially executable (reachable).
- `test`: This is not an actual goal implemented by any of the clients, but describes the collection of execution traces by a so-called Java agent that _dynamically instruments_ Java bytecode during JUnit and integration tests.
- `instr`: Produces a modified version of Java archives (_static instrumentation_) that can be deployed/executed in order to collect traces of actual method executions.
- `t2c`: Builds a call graph (starting from traced methods) and checks whether vulnerable code is potentially reachable from those.

The following goals are related to data management and reporting:

- `upload`: Uploads analysis data previously written to disk to the backend
- `report`: Downloads analysis data from the backend to the client, produces a result report (HTML, XML, JSON), and throws a build exception in order to break Jenkins jobs
- `clean`: Cleans the analysis data of a single app in the backend
- `cleanspace`: Cleans an entire workspace in the backend

Which goals are supported by the different clients, and how-to configure and execute them is explained in the following subsections.

Note that all goal executions (including configuration settings and statistics) are shown on the "History" tab of the respective applications.

**Important:** Make sure to understand the following before proceeding:

- `app` has to be executed before all the other analysis goals in order to detect all application dependencies with vulnerable code.
- Once it has been run, the [assessment of findings](../../manuals/assess_and_mitigate/#assessment-and-mitigation-manual) can already start, each finding of `app` shown on the "Vulnerabilities" tab corresponds to a dependency of an application on a component with a known security vulnerability. The number of findings will not change when running other analysis goals. Instead, `a2c`, `test` and `t2c` try to collect evidence concerning the potential or actual execution of vulnerable code brought up by `app`.
- Assess every finding, no matter whether `a2c`, `test` and `t2c` were able to collect evidence or not. Not finding such evidence does not mean that vulnerabilities cannot be exploited. The absence of proof is not a proof of absence (of exploitable vulnerabilities).

**Prerequisites:**

- A workspace has been created and its token is known
- Java 7 or later is installed
- Maven: The plugin is available in the local `.m2` repository or in a Nexus repository configured in `settings.xml` (see [here](https://maven.apache.org/guides/mini/guide-mirror-settings.html) for more information on how to configure Maven)

**Limitations**:

- The reachability analysis (goals `a2c` and `t2c`) does not work with Java 9, as the underlying analysis frameworks do not support it.

**Prerequisites**

- A workspace has been created and its token is known
- Java 7 or later is installed
- Maven: The @@PROJECT_NAME@@ Maven plugin must be available in the local `.m2` repository or in a Nexus repository configured in `settings.xml` (see [here](https://maven.apache.org/guides/mini/guide-mirror-settings.html) for more information on how to configure Maven)

!!! warning "Java 9 support"
    The reachability analysis (goals `a2c` and `t2c`) is not supported with Java 9, due to limitations of the  3rd-party analysis frameworks that @@PROJECT_NAME@@ relies upon.

## Bill of material analysis (app)

#### Objective

Create a complete bill of material (BOM) of the application and of all its dependencies (direct and transitive). Most importantly, the BOM comprises the signatures of all methods of the application and all dependencies, which is compared with a list of methods known to be vulnerable. Moreover, the BOM also comprises meta-info on archive level, e.g., the manifest file entries or the archive's digest (SHA1 in case of Java archives, MD5 in case of Python).

#### Result

In the @@PROJECT_NAME@@ frontend, the table in tab "Dependencies" is populated. In case any of the dependencies has vulnerabilities, they are shown in tab "Vulnerabilities". The column "Inclusion of vulnerable code" indicates whether the version in use is known to be vulnerable or not (see tooltip for more information).

#### Important

By default, the Maven plugin searches for application source and compiled code in the folders `src/main/java` and `target/classes`. If source or byte code is generated or modified during the build process, and stored in other folders than the ones mentioned, you need to add those directories using the parameter `vulas.core.app.sourceDir`. Otherwise, the respective code will not be recognized as application code, hence, ignored when performing the reachability analysis. Example: Suppose source code is generated into the folder `target/generated-sources`. If this code is compiled into the folder `target/classes`, you do not need to do anything. If it is compiled into a different folder, you would need to add this folder to entries of `vulas.core.app.sourceDir`.

#### Run as follows

```sh tab="CLI"
java -jar vulas-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal app
```

```sh tab="Maven"
mvn -Dvulas compile vulas:app
```

```sh tab="Gradle"
./gradlew assemble vulasApp
```

#### Configure as follows

```ini
# Where application source or bytecode and application dependencies (JAR and/or WAR files) are located
# Relative or absolute paths, multiple values to be separated by comma
vulas.core.app.sourceDir =

# Whether or not empty apps (w/o constructs and dependencies) are uploaded to the backend
vulas.core.app.uploadEmpty = false

# When true, JAR not known to @@PROJECT_NAME@@ Maven central and not already available to the backend are posted to the backend
vulas.core.app.uploadLibrary = false

# Number of worker threads analyzing the JAR files from which classes are loaded
vulas.core.jarAnalysis.poolSize = 4

# Package prefix(es) of application code (multiple values to be separated by comma)
# Default:
#   CLI: -
# Note: Ignored when running the Maven plugin. In all other cases it avoids the separation of application and dependency JARs into distinct folders
vulas.core.app.appPrefixes =

# Regex that identifies JARs with application code (multiple values to be separated by comma)
# Default:
#   CLI: -
# Note: Ignored when running the Maven plugin. In all other cases it avoids the separation of application and dependency JARs into distinct folders
vulas.core.app.appJarNames =
```

## Reachable from app (a2c)

#### Objectives

- Check whether vulnerable methods are reachable, i.e., whether the application can be run in a way that a vulnerable method is executed.
- Identify all so-called touch points, which are direct calls from an application method to a library method.
- Collect all reachable methods for every dependency of the application.

The first objective supports the risk assessment for a given vulnerability, while the second and third objectives primarily support the mitigation. Depending on the size of the application, the reachability analysis can consume a considerable amount of resources (time and memory). It is not seldom that it runs for several hours.

#### Limitations

- Python is not supported
- Java 9 and later versions are not supported by the underlying frameworks

#### Result

In the @@PROJECT_NAME@@ frontend, tab "Vulnerabilities", the column "Static Analysis" is populated for all libraries subject to known vulnerabilities. By selecting single row of this table and the one of the detailed page, one can get more information up until the paths of potential executions (if any).

#### How does it work

@@PROJECT_NAME@@ uses Wala or Soot, both static analysis frameworks for Java, in order to construct a call graph representing all possible program executions starting from application methods. This graph is traversed in order to see whether and from where methods with known vulnerabilities can be reached.

#### Run as follows

```sh tab="CLI"
java -Xmx8g -Xms2g -jar vulas-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal a2c
```

```sh tab="Maven"
export MAVEN_OPTS="-Xmx8g -Xms2g"
mvn -Dvulas compile vulas:a2c
```

```sh tab="Gradle"
./gradlew assemble vulasA2C
```

#### Configure as follows

```ini
# Limits the analysis to certain bugs (multiple values separated by comma)
# If empty, all relevant bugs retrieved from backend will be considered
# Default: empty
vulas.reach.bugs =

# Analysis framework to be used
# Possible values: wala, soot
vulas.reach.fwk = wala

# Regex to filter entry points (semicolon separated)
vulas.reach.constructFilter =

# All packages to be excluded from call graph construction, packages
# are separated by semicolon e.g. [java/.*;sun/.*]. Defaults for the different
# analysis frameworks are provided in the respective configuration files. -->
vulas.reach.excludePackages =

# All JAR files to be excluded from call graph construction (multiple entries to be separated by comma)
#
# Default: WebServicesAgent.jar (from Wily Introscope, an app perf monitoring tool that has invalid manifest header fields creating problems for Wala)
vulas.reach.excludeJars = WebServicesAgent.jar

# Dir to search for app source files (only vulas:a2c)
# If empty, they will be fetched from backend
vulas.reach.sourceDir =

# Timeout for reachability analysis (in mins)
# Default: 120 mins
vulas.reach.timeout = 120

# Max number of paths uploaded for a reachable change list element
vulas.reach.maxPathPerChangeListElement = 10

# Whether or not to collect touch points
# Default: true
vulas.reach.identifyTouchpoints = true

# Whether to search for the shortest path(s) from entry points to vulnerable constructs, or to quit after the first path found
# Default: true
vulas.reach.searchShortest = true
```

#### Call graph construction framework

Behind the scene, a source code analysis framework is used to construct the call graph, either starting from application methods (`a2c`) or from traced methods (`t2c`). Right now, the two frameworks [Wala](https://github.com/wala/WALA/wiki) and [Soot](http://www.sable.mcgill.ca/soot/) are supported and can be configured with `vulas.reach.fwk`. Both offer several configuration options to influence the accuracy of the call graph and its construction time. Once the call graph has been constructed, its size (in terms of nodes and edges) is printed to the console, which is useful for comparing the impact of the different configuration options, e.g.

```log
[vulas-reach-1] INFO  com.sap.psr.vulas.cg.wala.WalaCallgraphConstructor  - Normalized call graph has [167639 nodes] (with distinct ConstructId) and [1279495 edges]
```

#### WALA

The setting `vulas.reach.wala.callgraph.algorithm` determines the construction algorithm to be used. From `RTA` (Rapid Type Analysis) to `0-1-ctn-CFA`, the call graph  becomes more accurate, but the construction takes more time. A more accurate call graph means that it contains less false-positives, i.e., method invocations that cannot happen during actual program execution. As a rule of thumb, a call graph constructed with `RTA` contains more nodes and edges than one constructed with `0-1-ctn-CFA`. Note the following before choosing a more simple algorithm: The increase of nodes and edges resulting from, for instance, the choice of RTA, has a negative impact on the performance of the later analysis phases. As such, it may be worth to spend more time on the graph construction. See [here](https://ben-holland.com/call-graph-construction-algorithms-explained/), [there](http://wala.sourceforge.net/wiki/index.php/UserGuide:CallGraph) and [there](http://wala.sourceforge.net/wiki/index.php/UserGuide:PointerAnalysis) for more information regarding the difference of call graph construction algorithms.

```ini
# Possible values: 0-CFA; 0-ctn-CFA; vanilla-0-1-CFA; 0-1-CFA; 0-1-ctn-CFA
# Default algorithm: 0-1-CFA
vulas.reach.wala.callgraph.algorithm = 0-1-CFA
```

The setting `vulas.reach.wala.callgraph.reflection` determines the consideration of reflection, which is commonly used to instantiate and invoke classes and methods. See [here](https://github.com/wala/WALA/blob/master/com.ibm.wala.core/src/com/ibm/wala/ipa/callgraph/AnalysisOptions.java) for more information.

```ini
# Reflection option to be used for call graph construction
# Possible values: FULL; NO_METHOD_INVOKE; NO_STRING_CONSTANTS; APPLICATION_GET_METHOD
# Possible values: NONE; NO_FLOW_TO_CASTS; NO_FLOW_TO_CASTS_NO_METHOD_INVOKE; ONE_FLOW_TO_CASTS_NO_METHOD_INVOKE; NO_FLOW_TO_CASTS_APPLICATION_GET_METHOD; ONE_FLOW_TO_CASTS_APPLICATION_GET_METHOD
# Default value: NO_FLOW_TO_CASTS_NO_METHOD_INVOKE
vulas.reach.wala.callgraph.reflection = NO_FLOW_TO_CASTS_NO_METHOD_INVOKE
```

#### Soot

#### TODO

## Dynamic instrumentation (JUnit)

#### Objective

Collect method traces during the execution of JUnit tests. Information about traced methods will be compared with methods subject to known vulnerabilities.

#### Limitations

- Python is not supported

#### Result

- In the @@PROJECT_NAME@@ frontend, tab "Vulnerabilities", the column "Dynamic Analysis" is populated for all libraries subject to known vulnerabilities. By selecting single row of this table and the one of the detailed page, one can get more information up until the paths of actual executions (if any).
- In the @@PROJECT_NAME@@ frontend, tab "Test coverage", the number of traced vs. the number of total methods is shown per Java package of the application.

#### How does it work

@@PROJECT_NAME@@ collects runtime information during application execution, most importantly whether a vulnerable method has been called and the corresponding call stack. In order to collect this information, the byte code of the application and all its dependencies has to be changed, which can be achieved either dynamically or statically: In case of _dynamic instrumentation_, the byte code of a given Java class is changed at the time the class definition is loaded for the first time, e.g., during the execution of JUnit tests or integration tests. @@PROJECT_NAME@@ injects statements in order to save the timestamp of every method invocation as well as stack trace information. Per default, this information is saved in folder `target/vulas/upload` and uploaded using the goal `vulas:upload`. To that end, @@PROJECT_NAME@@ must be registered using the JVM option `-javaagent`. In case of JUnit tests, the agent is registered by the Maven goal `prepare-vulas-agent`. In case of _static instrumentation_, the byte code of classes residing in the file system is changed, e.g., the WAR file of a deployable Web application. This is done with help of the goal `vulas:instr` (see below).

#### Run as follows

```sh tab="Maven"
mvn -Dvulas vulas:prepare-vulas-agent test vulas:upload
```

#### Configure as follows

```ini
# Byte code instrumentor(s) to be used (multiple ones to be separated by comma)
#
# Possible values:
#   com.sap.psr.vulas.monitor.trace.SingleTraceInstrumentor: Collects exactly one timestamp for every invoked vulnerable method (no call stack)
#   com.sap.psr.vulas.monitor.trace.SingleStackTraceInstrumentor: Collects at most "vulas.core.instr.maxStacktraces" call stack for every invoked vulnerable method
#   com.sap.psr.vulas.monitor.trace.StackTraceInstrumentor:  Collects all call stacks for every invoked vulnerable method
#   com.sap.psr.vulas.monitor.touch.TouchPointInstrumentor: Collects so-called touch points, i.e., calls from an app method to a library method
#
# Default: com.sap.psr.vulas.monitor.trace.SingleTraceInstrumentor
#
# Note:
#   The above list of possible values is ordered ascending after performance impact and memory consumption,
#   i.e., the SingleTraceInstrumentor has the least impact on performance and memory consumption
vulas.core.instr.instrumentorsChoosen = com.sap.psr.vulas.monitor.trace.SingleTraceInstrumentor

# Max. number of stacktraces collected per instrumented vulnerable method
# Default: 10
# Note: Only applies to SingleStackTraceInstrumentor
vulas.core.instr.maxStacktraces = 10

# JARs in the following directories (or its subdirs) will not be instrumented
#vulas.core.instr.blacklist.dirs =

# Constructs of dependencies having one of the following scope(s) will not be instrumented (multiple ones to be separated by comma)
# Default: test, provided
# Note: Only applies to @@PROJECT_NAME@@ Maven plugin; in case of @@PROJECT_NAME@@ CLI, all dependencies have scope RUNTIME
vulas.core.instr.blacklist.jars.ignoreScopes = test, provided

# User-provided blacklist: Constructs of dependencies whose filename matches one of the following regular expressions will not be instrumented (multiple ones to be separated by comma)
# Default: -
# Note: Those are on top of "vulas.core.instr.blacklist.jars"
vulas.core.instr.blacklist.jars.custom =

# User-provided Java packages whose constructs are not instrumented (multiple ones to be separated by comma)
# Default: -
# Note: Those are on top of "vulas.core.instr.blacklist.classes.jre" and "vulas.core.instr.blacklist.classes"
vulas.core.instr.blacklist.classes.custom =

# If true, bytecode and instrumentation code will be written to tmpDir
vulas.core.instr.writeCode = false

# JARs for which no traces and no archive information will be uploaded (e.g., from @@PROJECT_NAME@@ itself)
# Multiple entries are separated by comma, each entry is a regex
vulas.core.monitor.blacklist.jars = lang-java-.*\.jar,vulas-core-.*\.jar,surefire-.*\.jar,junit-.*\.jar,org.jacoco.agent.*\.jar

# Enables or disables the periodic upload of collected traces to the backend
# Default: true
# Note: Set to FALSE in case of JUnit tests
vulas.core.monitor.periodicUpload.enabled = true

# Interval (in millisecs) between periodic uploads
# Default: 300000 (5 min)
vulas.core.monitor.periodicUpload.interval  = 300000

# Max. number of traces uploaded by each periodic upload
# Default: 1000
vulas.core.monitor.periodicUpload.batchSize = 1000

# Max number of items (traces, paths, touch points, etc.) collected
# Default: -1 (no limit)
vulas.core.monitor.maxItems = -1
```

## Dynamic instrumentation (JVM)

#### Objective

Collect method traces during the actual execution of the application. Information about traced methods will be compared with methods subject to known vulnerabilities.

#### Limitations

- Python is not supported

#### Result

Same as in previous section

#### How does it work

By registering a Java agent at JVM startup, @@PROJECT_NAME@@ changes the bytecode of every Java class loaded at runtime. For example, it injects Java statements in order to save the timestamp of every method invocation as well as stack trace information. This information is periodically uploaded to the @@PROJECT_NAME@@ backend.
#### Run as follows

- Download the file `lang-java-@@PROJECT_VERSION@@-jar-with-dependencies.jar` to your computer (CLI users can take it from the folder `./instr`).
- Add the following arguments to the Java runtime (and replace line breaks by a single space characters).

```text
-javaagent:lang-java-@@PROJECT_VERSION@@-jar-with-dependencies.jar
-Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend
-Dvulas.core.backendConnection=READ_WRITE
-Dvulas.core.monitor.periodicUpload.enabled=true
-Dvulas.core.appContext.group=<GROUP>
-Dvulas.core.appContext.artifact=<ARTIFACT>
-Dvulas.core.appContext.version=<VERSION>
-Dvulas.core.instr.instrumentorsChoosen=com.sap.psr.vulas.monitor.trace.SingleTraceInstrumentor
-Dvulas.core.space.token=<WORKSSPACE-TOKEN>
-noverify
```

- Start the application and perform some application-specific tests and workflows.

#### Example

In case of Tomcat 8.x, one needs to (1) copy `lang-java-@@PROJECT_VERSION@@-jar-with-dependencies.jar` into the folder `./bin` and (2) specify the variable `CATALINA_OPTS` as follows in the file `./bin/setenv.bat`. Do not forget to specify `<GROUP>`, `<ARTIFACT>` and `<VERSION>` for the application under analysis. Note: The use of `setenv.bat` does not work if Tomcat is run as Windows service.

```sh tab="CLI"
set "CATALINA_OPTS=-javaagent:lang-java-@@PROJECT_VERSION@@-jar-with-dependencies.jar -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend -Dvulas.core.backendConnection=READ_WRITE -Dvulas.core.monitor.periodicUpload.enabled=true -Dvulas.core.appContext.group=<GROUP> -Dvulas.core.appContext.artifact=<ARTIFACT> -Dvulas.core.appContext.version=<VERSION> -Dvulas.core.instr.instrumentorsChoosen=com.sap.psr.vulas.monitor.trace.SingleTraceInstrumentor -noverify"
```

## Static instrumentation (instr)

#### Objective

Modify an existing JAR (WAR) created by `mvn package` so that traces will be collected once the JAR is executed (the WAR is deployed in a Web application container such as Tomcat). Note: In contrast to what is described in the previous section "Integration Tests", `vulas:instr` will not result in the collection of traces for Tomcat itself.

#### Prerequisite

An application's JAR or WAR, e.g., as created with `mvn package` in folder `target`.

#### Limitations

- Python is not supported

#### Result

A new JAR/WAR with suffix `-vulas-instr` will be created in folder `target/vulas/target`.

#### How does it work

The bytecode of all the Java classes found in the JAR (WAR) will be modified as to collect information about, for instance, method execution and stack traces. This information will be uploaded to the backend if the JAR (WAR) is executed. Note: The modified code in the new JAR with suffix `-vulas-instr` can be inspected with decompilers such as [JD-GUI](http://jd.benow.ca/).

#### Run as follows

```sh tab="Maven"
mvn package
mvn -Dvulas vulas:instr
```

#### Troubleshooting

- The console shows compilation errors, e.g., `cannot find javax.servlet.http.HttpServletRequest`. The reason is that all application dependencies are re-compiled, and it can happen that some of the classes do have dependency requirements not met by the application. This can be overcome by identifying the respective JAR file and downloading it to the folder `target/vulas/lib`.

## Reachable from traces (t2c)

#### Objective

Understand whether vulnerable methods can be potentially reached from traced methods.

#### Prerequisite

Traces must have been collected during JUnit or integration tests (see above)

#### Limitations

- Python is not supported
- Java 9 and later versions are not supported by the underlying frameworks

#### Result

In the @@PROJECT_NAME@@ frontend, tab "Vulnerabilities", the column "Static Analysis" is populated for all libraries subject to known vulnerabilities. By selecting single row of this table and the one of the detailed page, one can get more information up until the paths of potential executions (if any).

#### How does it work

In contrast to the goal `a2c`, the callgraph is built starting from all methods that were previously traced. As such, the call graph construction overcomes weaknesses of static source analysis related to the use of reflection and control inversion. What remains the same is that the resulting graph is traversed in order to see whether and from where methods with known vulnerabilities can be reached.

#### Run as follows

```sh tab="CLI"
java -jar vulas-cli-jar-with-dependencies.jar -goal t2c
```

```sh tab="Maven"
mvn -Dvulas vulas:t2c
```

#### Configure as follows

```ini
# Limits the analysis to certain bugs (multiple values separated by comma)
# If empty, all relevant bugs retrieved from backend will be considered
# Default: empty
vulas.reach.bugs =

# Analysis framework to be used
# Possible values: wala, soot
vulas.reach.fwk = wala

# Regex to filter entry points (semicolon separated)
vulas.reach.constructFilter =

# All packages to be excluded from call graph construction, packages
# are separated by semicolon e.g. [java/.*;sun/.*]. Defaults for the different
# analysis frameworks are provided in the respective configuration files. -->
vulas.reach.excludePackages =

# All JAR files to be excluded from call graph construction (multiple entries to be separated by comma)
#
# Default: WebServicesAgent.jar (from Wily Introscope, an app perf monitoring tool that has invalid manifest header fields creating problems for Wala)
vulas.reach.excludeJars = WebServicesAgent.jar

# Dir to search for app source files (only vulas:a2c)
# If empty, they will be fetched from backend
vulas.reach.sourceDir =

# Timeout for reachability analysis (in mins)
# Default: 120 mins
vulas.reach.timeout = 120

# Max number of paths uploaded for a reachable change list element
vulas.reach.maxPathPerChangeListElement = 10

# Whether or not to collect touch points
# Default: true
vulas.reach.identifyTouchpoints = true

# Whether to search for the shortest path(s) from entry points to vulnerable constructs, or to quit after the first path found
# Default: true
vulas.reach.searchShortest = true
```

## Upload analysis files (upload)

#### Objective

Uploads analysis data in folder `vulas.core.uploadDir` to the backend. Such data is only written if the parameter `vulas.core.backendConnection` is set to `OFFLINE` or `READ_ONLY`. By default, this is only the case for the instrumentation of JUnit or integration tests.

#### Configure as follows

```ini
    # When true, serialized HTTP requests will be deleted after the upload succeeded (incl. the JSON files)
    # Default: true
    vulas.core.upload.deleteAfterSuccess = true
```

#### Run as follows

```sh tab="CLI"
java -Dvulas.core.appContext.group=<GROUP> -Dvulas.core.appContext.artifact=<ARTIFACT> -Dvulas.core.appContext.version=@@PROJECT_VERSION@@
     -jar vulas-cli-jar-with-dependencies.jar -goal upload
```

```sh tab="Maven"
mvn -Dvulas vulas:upload
```

## Create result report (report)

#### Objective

Creates result reports in HTML, XML and JSON format (on the basis of analysis results downloaded from the @@PROJECT_NAME@@ backend). Additionally, the Maven and Gradle plugins can be configured to throw a build exception in order break Jenkins jobs and pipelines in case vulnerable code is present (or reachable/executed). The HTML report can be copied into a Jenkins dashboard using the [HTML Publisher Plugin](http://wiki.jenkins-ci.org/display/JENKINS/HTML+Publisher+Plugin) (see [automation](../../tutorials/jenkins_howto) for a configuration example).

#### Multi-module Maven projects

The report goal should be called in a separate build step, e.g., `mvn -Dvulas vulas:report`. It must NOT be called together with other goals, e.g., `mvn -Dvulas compile vulas:app vulas:report`, because the build may fail before `app` and other goals are executed for all the modules. Alternatively, you can use the Maven option `--fail-at-end` (see [here](https://maven.apache.org/guides/mini/guide-multiple-modules.html) for more info).

#### Result

A summary report is written to disk (in HTML, XML and JSON format). For Maven, the target directory of the different files is "target/vulas/report", for Gradle it is "build/vulas/report". For CLI, the exact location is printed to the console.

#### How does it work

Identified vulnerabilities including any information gathered during static and dynamic analysis will be downloaded from the backend.

#### Configure as follows

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

    # Determines whether un-assessed vulnerabilities (e.g. vulnerabilities marked with an orange hourglass symbol) throw a build exception. Un-assessed vulns are those where
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

    # Specifies whether a build exception is thrown when vulnerable code is included, potentially
    # reachable, actually reached or not at all
    # Possible values: noException < dependsOn < potentiallyExecutes < actuallyExecutes
    #    noException : no build exception even if vulnerable code is included
    #    dependsOn : exception raised when vulnerable code is included
    #    potentiallyExecutes : exception raised when vulnerable code is potentially executed (result of static analyse)
    #    actuallyExecutes : exception raised when vulnerable code is executed (result of dynamic analyse)
    #
    # Default: actuallyExecutes
    vulas.report.exceptionThreshold = actuallyExecutes

    # Directory to where the reports (JSON, XML, HTML) will be written to
    # Default:
    #   CLI: -
    #   MVN: ${project.build.directory}/vulas/report
    vulas.report.reportDir =
```

#### Run as follows

```sh tab="CLI"
java -Dvulas.core.appContext.group=<GROUP> -Dvulas.core.appContext.artifact=<ARTIFACT> -Dvulas.core.appContext.version=@@PROJECT_VERSION@@
     -jar vulas-cli-jar-with-dependencies.jar -goal report
```

```sh tab="Maven"
mvn -Dvulas vulas:report
```

```sh tab="Gradle"
./gradlew vulasReport
```

#### Exemptions

The settings `vulas.report.exceptionExcludeBugs` and `vulas.report.exceptionExcludeBugs.<vuln-id>` can be used to capture the results of an audit or assessment by developers in regards to whether a vulnerability is problematic in a given application context. Exempted bugs do not result in build exceptions and are also shown in the apps Web frontend.

#### Build exceptions

Other settings to fine-tune the threshold for build exceptions are as follows:

- `vulas.report.exceptionScopeBlacklist` can be used to exclude certain Maven scopes (default: test)
- `vulas.report.exceptionThreshold` can be used to specify whether a build exception is thrown when vulnerable code is included, potentially reachable, actually reached or not at all (values: `noException`, `dependsOn`, `potentiallyExecutes`, `actuallyExecutes`; default: `actuallyExecutes`)

## Clean and delete apps (clean)

#### Objective

Deletes application-specific data in the backend, e.g., traces collected during JUnit tests, or application constructs and dependencies collected through the `app` goal. Right after executing `clean` for a given application, the apps Web frontend will be empty for the respective application.

#### Configure as follows

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

Run as follows to **clean the current version**:

```sh tab="CLI"
java -jar vulas-cli-jar-with-dependencies.jar -goal clean
```

```sh tab="Maven"
mvn -Dvulas vulas:clean
```

Run as follows to **delete an application including all its versions**:

```sh tab="CLI"
java -Dvulas.core.clean.purgeVersions=true -Dvulas.core.clean.purgeVersions.keepLast=0 -jar vulas-cli-jar-with-dependencies.jar -goal clean
```

```sh tab="Maven"
mvn -Dvulas -Dvulas.core.clean.purgeVersions=true -Dvulas.core.clean.purgeVersions.keepLast=0 vulas:clean
```

!!! critical Troubleshooting
    Maven will fail to delete an application if a corresponding `<module>` does not exit any longer in the `pom.xml`. The CLI must be used in these cases and the Maven coordinates (GAV) of the item to be cleaned shall be provided as system properties when calling the CLI. For example, if you want to delete an application with GAV `myGroup:myArtifact:myVersion`, the following command line should be used

    ```sh
    java -Dvulas.core.clean.purgeVersions=true -Dvulas.core.clean.purgeVersions.keepLast=0 -Dvulas.core.appContext.group=myGroup -Dvulas.core.appContext.artifact=myArtifact -Dvulas.core.appContext.version=myVersion -jar vulas-cli-jar-with-dependencies.jar -goal clean
    ```

## Clean workspaces (cleanspace)

#### Objective

Deletes all applications of the given space.

#### Run as follows

```sh tab="CLI"
java -jar vulas-cli-jar-with-dependencies.jar -goal cleanSpace
```

```sh tab="Maven"
mvn -Dvulas vulas:cleanSpace
```
