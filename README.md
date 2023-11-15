<p align="center">
    <a href="https://eclipse.github.io/steady/">
        <img height="64" src="docs/public/content/images/ES-logo-152-transparent.png">
    </a>
</p>

# Eclipse Steady (Incubator Project)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE.txt)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.eclipse.steady/plugin-maven/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.eclipse.steady/plugin-maven)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/4202/badge)](https://bestpractices.coreinfrastructure.org/projects/4202)
[![REUSE status](https://api.reuse.software/badge/github.com/eclipse/steady)](https://api.reuse.software/info/github.com/eclipse/steady)

**Discover, assess and mitigate known vulnerabilities in your Java projects**

Eclipse Steady supports software development organizations in regards to the secure use of open-source components during application development. The tool analyzes **Java** applications in order to:

- detect whether they depend on open-source components with known vulnerabilities,
- collect evidence regarding the execution of vulnerable code in a given application context (through the combination of static and dynamic analysis techniques), and
- support developers in the mitigation of such dependencies.

As such, it addresses the OWASP Top 10 security risk A6, [Vulnerable and Outdated Components](https://owasp.org/Top10/A06_2021-Vulnerable_and_Outdated_Components/), which is often the root cause of data breaches: [snyk.io/blog/owasp-top-10-breaches](https://snyk.io/blog/owasp-top-10-breaches/)

In comparison to other tools, the detection is code-centric and usage-based, which allows for more accurate detection and assessment than tools relying on meta-data.  It is a collection of client-side scan tools, microservices and rich [OpenUI5](https://openui5.hana.ondemand.com/) Web frontends.

## Disclaimer

Please note the following:
- Steady can only find vulnerabilities maintained in [Project KB](https://github.com/sap/project-kb), which is Steady's only source of vulnerability information. The reason is that only Project KB provides information about fix commits in a systematic way and in machine readable format. As of September 2022, Project KB contains roughly 700+ vulnerabilities, with little coverage of the years 2021 and later.
- The [ideal Steady setup](https://eclipse.github.io/steady/admin/tutorials/docker/) is to run the Docker Compose application on an internal cloud, with CI/CD systems and developer work stations connecting to that instance. This makes the operation of Steady more heavy-weight compared to other open source vulnerablity scanners like [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/). This architecture comes with some advantages and disadvantages. For example, scan results from different systems are uploaded to such central server, which offers a user interface with various features to explore application dependencies and understand vulnerability exploitability. On the other hand, the architecture makes it more difficult to run some quick tests.

As a consequence, we recommend Steady primarily for organizations that can afford (a) hosting Steady on their internal cloud and (b) dedicating resources to contribute to the vulnerability information in [Project KB](https://github.com/sap/project-kb).

## Quickstart

This section provides the bare minimum to setup Steady and to use its Maven plugin for scanning a Java application.

1. The Steady **backend**, a Docker Compose application, stores information about open-source vulnerabilities and scan results. It has to be installed once, ideally on a dedicated host, and must be running during application scans.

    Download and run [`setup-steady.sh`](https://raw.githubusercontent.com/eclipse/steady/master/docker/setup-steady.sh) to install the backend on any host with a recent version of Docker/Docker Compose (the use of profiles requires a version >= 1.28, installable with `pip install docker-compose` or as [described here](https://github.com/docker/compose#where-to-get-docker-compose)).

    **Notes**:
    - Tested with Docker 20.10.11 + Docker Compose 1.29.2 on Intel Macs with macOS 12.3.1, and Docker 20.10.15 + Docker Compose 1.29.0 on Ubuntu 20.04.4 and 18.04.6.
    - During its first execution, triggered by the setup script triggered by `setup-steady.sh` or directly using `start-steady.sh -s ui`, the backend will be bootstrapped by downloading and processing code-level information of hundreds of vulnerabilities maintained in the open-source knowledge base [Project KB](https://github.com/sap/project-kb). While the bootstrapping can take up to two hours, later updates will import the delta on a daily basis. Run `start-steady.sh -s none` to shut down all Docker Compose services of the backend.

2. A Steady **scan client**, e.g. the Maven plugin, analyzes the code of your application project and its dependencies. Being [available on Maven Central](https://search.maven.org/search?q=g:org.eclipse.steady), the clients do not require any installation. However, they need to be run whenever your application's code or dependencies change.

    In case application scan and Steady backend run on different hosts, the scan clients must be configured accordingly. Just copy and adjust the file `~/.steady.properties`, which has been created in the user's home directory during the backend setup.

    For Maven, `cd` into your project and run the `app` analysis goal as follows (see [here](https://eclipse.github.io/steady/user/manuals/analysis/) for more information about available goals):

    `mvn org.eclipse.steady:plugin-maven:3.2.5:app`

    **Note**: During application scans, a lot of information about its dependencies is uploaded to the backend, which makes that the first scan takes significantly more time than later scans of the same application.

## History

[Originally developed](https://scholar.google.com/citations?user=FOEVZyYAAAAJ&hl=en) by [SAP Security Research](https://www.sap.com/documents/2017/12/cc047065-e67c-0010-82c7-eda71af511fa.html), the tool has been productively used at SAP between late 2016 and April 2021. As of April 2017, the tool was the officially recommended open-source scan solution for Java (and then Python) applications at SAP. As of April 2019, it has been used to perform 1M+ scans of ~1000 Java and Python development projects.

The tool approach is best described in the following scientific papers, please cite these if you use the tool for your research work:

- [Serena Ponta](https://scholar.google.com/citations?hl=en&user=DFVwF6sAAAAJ), [Henrik Plate](https://scholar.google.com/citations?user=Kaleo5YAAAAJ&hl=en), [Antonino Sabetta](https://scholar.google.com/citations?hl=en&user=BhcceV8AAAAJ), [**Detection, assessment and mitigation of vulnerabilities in open source dependencies**](https://link.springer.com/article/10.1007/s10664-020-09830-x), Empirical Software Engineering, volume 25, pages 3175–3215 (2020)
- [Serena Ponta](https://scholar.google.com/citations?hl=en&user=DFVwF6sAAAAJ), [Henrik Plate](https://scholar.google.com/citations?user=Kaleo5YAAAAJ&hl=en), [Antonino Sabetta](https://scholar.google.com/citations?hl=en&user=BhcceV8AAAAJ), [**Beyond Metadata: Code-centric and Usage-based Analysis of Known Vulnerabilities in Open-source Software**](https://arxiv.org/abs/1806.05893), 34th International Conference on Software Maintenance and Evolution (ICSME), 2018
- [Henrik Plate](https://scholar.google.com/citations?user=Kaleo5YAAAAJ&hl=en), [Serena Ponta](https://scholar.google.com/citations?hl=en&user=DFVwF6sAAAAJ), [Antonino Sabetta](https://scholar.google.com/citations?hl=en&user=BhcceV8AAAAJ), [**Impact Assessment for Vulnerabilities in Open-Source Software Libraries**](https://arxiv.org/pdf/1504.04971.pdf), 31st International Conference on Software Maintenance and Evolution (ICSME), 2015

## Features

- **Detection of vulnerable code** is realized by discovering method signatures in Java archives and comparing their source and byte code with the vulnerable and fixed version (as known from the fix commit). As such, the detection is more accurate than for approaches based on meta-data (less false-positives and false-negatives). In particular, it is robust against rebundling, a very common practice in the Java ecosystem.
- **Assessment of vulnerable dependencies** by application developers and security experts is supported by information about the potential and actual execution of vulnerable code. This information is based on call graph analysis and trace information collected during JUnit and integration tests. Going down to the granularity of single methods, application developers are presented with the potential and actual call stack from application code till vulnerable code.
- The addition of new vulnerabilities to the knowledge base does not require the re-scan of applications. In other words, right after  an addition to the knowledge base, it is immediately known whether previously scanned applications are affected or not.
- **Mitigation proposals** consider the _reachable_ share of dependencies, i.e., the set of methods that can be potentially reached from application code union the actual executions observed during tests. This information is used to compute several metrics aiming to let developers chose the best non-vulnerable replacement of a vulnerable dependency (best in regards to non-breaking and with least regression likelihood).
- Individual findings can be exempted if developers come to the conclusion that a vulnerability cannot be exploited in a given application-context. This information can be maintained in an auditable fashion (incl. timestamp and author information) and typically prevents build exceptions during CI/CD pipelines.
- Organization-internal CERTs can query for all applications affected by a given vulnerability. This feature supports, for instance, larger development organizations with many software applications developed by distributed and de-central development units.

## Requirements

Eclipse Steady has a distributed architecture composed of a couple of Spring Boot microservices, two Web frontends and a number of client-side scanners/plugins, which perform the actual analysis of application and dependency code on build systems or developer workstations.

To build/test the entire project, the following tools are needed:

- **[JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/index.html)**
- **[Maven 3.3+](https://maven.apache.org/download.cgi)** for the analysis of Maven projects using [`plugin-maven`](https://github.com/eclipse/steady/tree/master/plugin-maven)
- **[Python 3](https://www.python.org/downloads/)** as well as the packages `pip`, `virtualenv` and `setuptools` (`pip install -r requirements.txt`) for the analysis of Python applications using [`cli-scanner`](https://github.com/eclipse/steady/tree/master/cli-scanner)
- **[Gradle 4](https://gradle.org/install/)** for the analysis of Gradle projects using [`plugin-gradle`](https://github.com/eclipse/steady/tree/master/plugin-gradle).

## Build and Test

Eclipse Steady is built with Maven. The module `lang-python` requires Python 3 to be installed. To enable the support for Gradle the profile `gradle` needs to be activated (`-P gradle`).

```sh
mvn clean install
```

During the `install`ation phase of `mvn` all the tests are run. Long-running tests can be disabled with the flag `-DexcludedGroups=org.eclipse.steady.shared.categories.Slow`.

## Limitations

Due to the current lack of an authentication and authorization mechanism, it is NOT recommended to run the Web frontends and server-side microservices on systems accessible from the Internet.

Other limitations:

- Static and dynamic analyses are not implemented for Python
- Java 9 multi-release archives are not supported (classes below `META-INF/versions` are simply ignored)

## Acknowledgement 

This work is partly funded by the EU under the H2020 research project [SPARTA](https://sparta.eu/) (Grant No.830892).

[**Documentation**](https://eclipse.github.io/steady/user/) · [**Support**](https://eclipse.github.io/steady/user/support/) · [**Contributing**](https://eclipse.github.io/steady/contributor/) · [**Deploy guide**](https://eclipse.github.io/steady/admin/tutorials/docker/) · [**Scan guide**](https://eclipse.github.io/steady/user/tutorials/) · [**Vulnerability database**](https://eclipse.github.io/steady/vuln_db/) · [**Blog**](https://blogs.sap.com/tag/vulas/)
