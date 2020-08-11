<p align="center">
    <a href="https://eclipse.github.io/steady/">
        <img height="64" src="docs/public/content/images/ES-logo-152-transparent.png">
    </a>
</p>

# Eclipse Steady (Incubator Project)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE.txt) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md) [![Build Status](https://travis-ci.org/eclipse/steady.svg?branch=master)](https://travis-ci.org/eclipse/steady) [![Release](https://img.shields.io/github/release/eclipse/steady.svg)](https://github.com/eclipse/steady/releases)

**Discover, assess and mitigate known vulnerabilities in your Java and Python projects**

Eclipse Steady supports software development organizations in regards to the secure use of open-source components during application development. The tool analyzes **Java** and **Python** applications in order to:

- detect whether they depend on open-source components with known vulnerabilities,
- collect evidence regarding the execution of vulnerable code in a given application context (through the combination of static and dynamic analysis techniques), and
- support developers in the mitigation of such dependencies.

As such, it addresses the OWASP Top 10 security risk A9, [Using Components with Known Vulnerabilities](https://www.owasp.org/index.php/Top_10-2017_A9-Using_Components_with_Known_Vulnerabilities), which is often the root cause of data breaches: [snyk.io/blog/owasp-top-10-breaches](https://snyk.io/blog/owasp-top-10-breaches/)

In comparison to other tools, the detection is code-centric and usage-based, which allows for more accurate detection and assessment than tools relying on meta-data.  It is a collection of client-side scan tools, microservices and rich [OpenUI5](https://openui5.hana.ondemand.com/) Web frontends.

Read more in our [**Docs**](https://eclipse.github.io/steady/)

## History

[Originally developed](https://scholar.google.com/citations?user=FOEVZyYAAAAJ&hl=en) by [SAP Security Research](https://www.sap.com/documents/2017/12/cc047065-e67c-0010-82c7-eda71af511fa.html), the tool is productively used at SAP since late 2016 (but an earlier prototype was available since 2015). In April 2017, the tool became the officially recommended open-source scan solution for Java (and then Python) applications at SAP. As of April 2019, it has been used to perform 1M+ scans of ~1000 Java and Python development projects, and its adoption is growing at a steady pace.

The tool approach is best described in the following scientific papers, please cite these if you use the tool for your research work:

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
- **[Maven 3.3+](https://maven.apache.org/download.cgi)** for the analysis of Maven projects using [`plugin-maven`](https://github.com/SAP/vulnerability-assessment-tool/tree/master/plugin-maven)
- **[Python 3](https://www.python.org/downloads/)** as well as the packages `pip`, `virtualenv` and `setuptools` (`pip install -r requirements.txt`) for the analysis of Python applications using [`cli-scanner`](https://github.com/SAP/vulnerability-assessment-tool/tree/master/cli-scanner)
- **[Gradle 4](https://gradle.org/install/)** for the analysis of Gradle projects using [`plugin-gradle`](https://github.com/SAP/vulnerability-assessment-tool/tree/master/plugin-gradle).

## Build and Test

Eclipse Steady is built with Maven. To enable the support for Gradle the profile `gradle` needs to be activated (`-P gradle`)

```sh
mvn clean install
```

During the `install`ation phase of `mvn` all the tests are run. Long-running tests can be disabled with the flag `-DexcludedGroups=com.sap.psr.vulas.shared.categories.Slow`. All the tests can be disabled with the flag `-DskipTests`.

## Limitations

Due to the current lack of an authentication and authorization mechanism, it is NOT recommended to run the Web frontends and server-side microservices on systems accessible from the Internet.

Other limitations:

- Static and dynamic analyses are not implemented for Python
- Static analysis for Java is only supported until Java 8
- Java 9 multi-release archives are not supported (classes below `META-INF/versions` are simply ignored)

## Todo (upcoming changes)

The following is a subset of pending feature requests:

- Static and dynamic analysis for Python
- Support of JavaScript (client- and server-side)
- UI dashboards for workspaces

[**Documentation**](https://eclipse.github.io/steady/user/) · [**Support**](https://eclipse.github.io/steady/user/support/) · [**Contributing**](https://eclipse.github.io/steady/contributor/) · [**Deploy guide**](https://eclipse.github.io/steady/admin/tutorials/docker/) · [**Scan guide**](https://eclipse.github.io/steady/user/tutorials/) · [**Vulnerability database**](https://eclipse.github.io/steady/vuln_db/) · [**Blog**](https://blogs.sap.com/tag/vulas/) · [![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/2605/badge)](https://bestpractices.coreinfrastructure.org/projects/2605)

## License

Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.

This project is licensed under the Apache Software License, v.2 except as noted otherwise in the [LICENSE file](LICENSE.txt).
