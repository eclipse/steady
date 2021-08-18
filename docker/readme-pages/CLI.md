## Prerequisites

- The token of a Steady [workspace](Workspace.md) is known
- A working installation of the JDK

## Setup

1. Create a new folder, and structure it as follows:

| Folder/File | Description |
|---|---|
| `./app/` | Create folder `app` and put the application code (java, class or JAR files) and all application dependencies (JAR files) into this folder. It will be searched recursively, thus, it is possible to just copy the entire installation directory of an application into the folder. **Important**: (1) Single java and class files are always considered as application code, no matter the package prefix configured with ```vulas.core.app.appPrefixes```. (2) JARs are always considered as application dependency unless they only contain methods starting with the configured package prefix. (3) Nested JARs must be extracted, WARs can stay as-is. |
| `./steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar` | Copy and rename the executable JAR from `<vulas-root>/cli-scanner/target/cli-scanner-<version>-jar-with-dependencies.jar'. It is used to run the different Steady goals. |
| `./instr/lang-java-<version>-jar-with-dependencies.jar` | Copy the executable JAR from `<vulas-root>/lang-java/target/`. It is used to instrument Java runtimes. |
| `./vulas-custom.properties` | Create the file for the configuration settings for Steady and fill it with the content below. **Important**: (1) Specify `<GROUP>`, `<ARTIFACT>` and `<VERSION>` of the application to be analyzed. (2) Specify how Steady identifies your application code (either using `vulas.core.app.appPrefixes` or `vulas.core.app.appJarNames`, see below for more information). (3) Specify the workspace token `vulas.core.space.token`. (4) Provide the hostname of the vulas backend (replacing localhost). |

vulas-custom.properties
```vulas-custom.properties
# vulas.core.space.token = <YOUR WORKSPACE TOKEN>
vulas.shared.backend.serviceUrl=http://localhost:8033/backend
vulas.shared.tmpDir = vulas/tmp

vulas.core.appContext.group = <GROUP>
vulas.core.appContext.artifact = <ARTIFACT>
vulas.core.appContext.version = <VERSION>

# One or more dirs with app constructs (JAR, class, java), comma-separated
vulas.core.app.sourceDir = app

# One or more package prefixes for code belonging to SAP (typically com.sap), multiple entries to be comma-separated
vulas.core.app.appPrefixes = com.sap 

vulas.core.uploadEnabled = true

vulas.reach.wala.callgraph.reflection = NO_FLOW_TO_CASTS_NO_METHOD_INVOKE
vulas.reach.timeout = 120

vulas.core.instr.sourceDir =
vulas.core.instr.targetDir = vulas/target
vulas.core.instr.includeDir = vulas/include
vulas.core.instr.libDir = vulas/lib
vulas.core.instr.instrumentorsChoosen = org.eclipse.steady.java.monitor.trace.SingleTraceInstrumentor
vulas.core.instr.searchRecursive = true

vulas.report.reportDir = vulas/report
vulas.report.exceptionExcludeBugs =
```

**Identification of application code**: You can use `vulas.core.app.appPrefixes` or `vulas.core.app.appJarNames` to tell Steady how to identify the code of your application, which is important for the call graph construction during the A2C reachability analysis. This analysis is not complete if not all the relevant application methods are used as entry points for the call graph construction. As such, the potential execution of vulnerable open-source methods may be missed. A good indicator to see whether specification is correct is to see whether there are items in the Dependencies tab that are created by you (or your organization), or whether there are open-source packages mentioned in the table on the Statistics tab.

```
# Package prefix(es) of application code (multiple values to be separated by comma), only relevant for CLI
vulas.core.app.appPrefixes = 

# Regex that identifies JARs with application code (multiple values to be separated by comma), only relevant for CLI
vulas.core.app.appJarNames =
```

## Goal execution

### app

1. Run `java -jar steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal app`.

2. Connect to the apps Web frontend, then select your workspace and application.

### a2c

1. Run `java -jar steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal a2c`.

2. Connect to the apps Web frontend, then select your workspace and application. Please notice that in order to have the a2c goal working, user needs to copy the jar file(s) of his/her application into the 'app' folder.

### clean

1. Run `java -jar steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal clean`.

### report

1. Run `java -jar steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal report`.

2. Check the console to see where the Html, Json and Xml reports have been written to.

### test

Important: The CLI does not offer the 'test' goal known from Maven. The simple reason is that 'test' triggers the ```maven-surefire-plugin``` to execute JUnit test cases, none of which exists outside the context of a Maven project. In order to collect traces, one has to instrument the Java runtime (see section integration tests).

### Integration tests

In order to collect traces outside of the context of Gradle or Maven projects, one can instrument the Java runtime as follows.

1. Add the following arguments to the Java call starting your application (replace placeholders and line breaks by a single space characters).
```
    -javaagent:./instr/lang-java-<version>-jar-with-dependencies.jar
    -Dvulas.shared.backend.serviceUrl=http://localhost:8033/backend
    -Dvulas.core.space.token=<YOUR WORKSPACE TOKEN>
    -Dvulas.core.backendConnection=READ_WRITE
    -Dvulas.core.monitor.periodicUpload.enabled=true
    -Dvulas.core.appContext.group=<GROUP>
    -Dvulas.core.appContext.artifact=<ARTIFACT>
    -Dvulas.core.appContext.version=<VERSION>
    -Dvulas.core.instr.instrumentorsChoosen=org.eclipse.steady.java.monitor.trace.SingleTraceInstrumentor
    -noverify
```

**Tomcat 8.x**: Copy `lang-java-@@PROJECT_VERSION@@-jar-with-dependencies.jar` into the Tomcat folder `./bin/instr`, and set the variable `CATALINA_OPTS` as follows in `./bin/setenv.bat` (take all arguments from above and don't forget to replace line breaks and placeholders). Note: The use of `setenv.bat` does not work if Tomcat is run as Windows service.

```
set "CATALINA_OPTS=-javaagent:./instr/lang-java-<version>-jar-with-dependencies.jar  <...>"
```

2. Open the application and perform some application-specific tests and workflows.

### t2c

1. Run `java -jar steady-cli-@@PROJECT_VERSION@@-jar-with-dependencies.jar -goal t2c`.

2. Connect to the apps Web frontend, then select your workspace and application. Please notice that in order to have the t2c goal working, user needs to copy the jar file(s) of his/her application into the 'app' folder.
