## Jenkins

A typical Jenkins job comprises the following steps:
1. Execute _clean_ in order to delete existing analysis results of the application, e.g., `mvn -Dvulas vulas:clean`.
2. Execute a sequence of analysis goals, e.g., `mvn -Dvulas clean compile vulas:app`. **Nightly jobs** should include the resource-consuming goals _a2c_, _test_ and _t2c_, while **commit-triggered jobs** should only include _app_.
3. Create a report, e.g., `mvn -Dvulas vulas:report`. This goal is executed separately from the analysis goals. Otherwise, in case of multi-module Maven projects, it may throw a build exception before all modules have been analyzed.
4. Use the [HTML Publisher Plugin](https://wiki.jenkins-ci.org/display/JENKINS/HTML+Publisher+Plugin) for Jenkins to copy the Html report created by vulas:report into the Jenkins dashboard. As such, Vulas results can be consumed w/o the need to scroll through the very verbose console output.


## Example

All analysis goals can be run in a single Maven call. The _report_ goal is called separately, as a build exception in one module (due to vulnerable dependencies) would prevent the analysis of subsequent modules. The following calls assume the use of the [Vulas profile](Sample-Maven-profile.md).

```
mvn -Dvulas clean compile vulas:clean vulas:app vulas:a2c test vulas:upload vulas:t2c
mvn -Dvulas vulas:report
```

The following screenshot shows a corresponding Jenkins job whereby the result report is copied into the Jenkins dashboard using the HTML Publisher plugin.

![](jenkins.png)

## Troubleshooting

**Symptom**: The Vulas Html report is not properly rendered inside Jenkins (e.g., no images, no color-coding, no JavaScript).

**Solution**: Either (a) download a ZIP of the Vulas report (the link can be found in the upper-right corner), or (b) adopt the content security policy (CSP) of Jenkins as described [here](https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy). In the latter case, you should execute the following command in the Jenkins script console:

`System.setProperty("hudson.model.DirectoryBrowserSupport.CSP","sandbox allow-scripts; default-src 'none'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; script-src 'unsafe-inline'")`
