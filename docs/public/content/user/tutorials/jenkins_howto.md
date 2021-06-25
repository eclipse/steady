# Jenkins automation

## Quick vs. deep scans

CI/CD jobs configured with Jenkins or Travis can be triggered in many different ways, e.g., on-commit or periodically, which decides about the frequency of scan jobs.

**Quick scans (without reachability analysis):** If scan jobs are expected to run very frequently, e.g., after every commit in the application's source code repository, it is preferable to only execute the `app` analysis goal, which typically does not take more than a couple of minutes (at most). Such a quick scan detects the very same number of vulnerabilities than a deep scan, but does not collect any information about the reachability of vulnerable code.

```Bash tab="Maven"
mvn -Dsteady clean compile steady:clean steady:app
```

**Deep scans (with reachability analysis):** Scan jobs that run once a day or less can include analysis goals such as `a2c`, `test` and `t2c`. The static analysis goals `a2c` and `t2c`, in particular, can take a considerable amount of time until completion (up to several hours), depending on the complexity of the application project under analysis (number of modules, number of application constructs, etc.):

```Bash tab="Maven"
mvn -Dsteady clean compile steady:clean steady:app steady:a2c steady:prepare-vulas-agent package steady:upload steady:t2c
```

See [here](../../manuals/analysis/) for more information on @@PROJECT_NAME@@ goals.

## Jenkins

A typical Jenkins job configuration using the @@PROJECT_NAME@@ plugin for Maven comprises the following two build steps and one post-build action (see screenshot below):

1. Build step for a **quick scan** or **deep scan**, depending on the expected run frequency.
2. Build step `-Dsteady steady:report` to create result reports (per default in folder `target/vulas/result`).
3. Post-build action with [HTML Publisher Plugin](https://wiki.jenkins.io/display/JENKINS/HTML+Publisher+Plugin) to copy the Html report created by `report` into the Jenkins dashboard. As such, @@PROJECT_NAME@@ results can be consumed w/o the need to scroll through the verbose console output.

Additional notes:

* The above assumes that the @@PROJECT_NAME@@ Maven profile is present in the project's `pom.xml`.

!!! info "The `report` goal should always be run in a separate Maven invocation."
    Otherwise, in case of multi-module Maven projects, `report` may throw a build exception before all of the modules have been analyzed.

<center class='expandable'>
    [![alt](img/jenkins.jpg)](img/jenkins.jpg)
</center>
