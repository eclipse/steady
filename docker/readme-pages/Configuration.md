## Configuration layers

The client-side analysis tools can be configured in different ways. At runtime, the following layers are combined in order to establish the effective configuration (which is printed to console upon goal execution).
- Java system properties: Can be specified when running `java` or `mvn`, each setting must be prefixed with `-D`
- Property files in file system: Can be specified by adding property file(s) in the folder where Vulas is executed (or any of its subfolders). The file name must adhere to the format `vulas-*.properties`, and its entries must be specified according to the [Java spec](https://en.wikipedia.org/wiki/.properties)
- Plugin configuration:
    * Maven: Can be specified in the `<layeredConfiguration>` section of the Vulas Maven plugin
    * Gradle: todo
- Environment variables: Can be specified using `export` (*nix) and `set` (Windows)
- Property files in Vulas JAR files: Default values for many settings are embedded inside the Vulas JAR files

## General settings

Many settings are specific to the individual goals and are explained in the respective [Java](Java.md) and [Python](Python.md) sections.

The following settings, however, have to be present for every goal execution:
- `vulas.core.space.token`: The token of the workspace to be used for the analysis
- `vulas.core.appContext.group`, `vulas.core.appContext.artifact` and `vulas.core.appContext.version`: Altogether, the uniquely identify an application within a space. Depending on the client used, one or more of them are automatically inferred, e.g., using data from `pom.xml` or `build.gradle`.
- `vulas.shared.backend.serviceUrl`: The URL of the backend service to which clients upload analysis results.

## Check setup

Proceed as follows to check whether the Vulas setup and goal execution works:

**On the client**, after executing a specific Vulas [goal](Goals) on your application, a log entry similar to the one below should be printed to the console. It is used to upload goal-related information to the Vulas backend, e.g., the Vulas version used or the average memory consumption. In this example, information related to a goal execution for an application with GAV `com.acme.foo:vulas-testapp-webapp:3.0.9-MVN` was uploaded to `http://<host>:8080/backend`, workspace `78E0EA14A6C0EF33ADEC9111B7088CE4`. The successful upload is visible from the HTTP response code `201`.

```
[main] INFO  com.sap.psr.vulas.backend.requests.BasicHttpRequest  - HTTP POST [uri=http://<host>:8080/backend/apps/com.acme.foo/vulas-testapp-webapp/3.0.9-MVN/goals, size=23,50 KB, tenant=603EFBA1EA9B98ADB4B548682597E6D0, space=78E0EA14A6C0EF33ADEC9111B7088CE4]
[main] INFO  com.sap.psr.vulas.backend.requests.BasicHttpRequest  - HTTP POST completed with response code [201] in [03.019 ms] (proxy=false)
```

**In the apps Web frontend** at `https://<host>/apps`, there are several tabs to be checked:
* On the Dependencies tab, one should see all application dependencies. In particular, there should be no archives belonging to the application under analysis (which can happen if the CLI is not configured to properly separate application code and dependency code, see [here](CLI.md) for more information).
* On the Statistics tab, one should see all packages belonging to the application. In particular, there should be no packages belonging to 3rd party / open-source libraries (which can happen if the CLI is not configured to properly separate application code and dependency code, see [here](CLI.md) for more information).
* On the History tab, one should see table entries for all goal executions that happened on the client.
