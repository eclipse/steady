<p align="center"><img height="64" src="./docs/media/images/logo/vulas.png"></p>

# Open-source vulnerability assessment tool [![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE.txt) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md) <!-- [![Build Status](https://<host>/job/<name>/job/<job-name>/lastBuild/badge/icon)](https://<host>/job/<name>/job/<job-name>/lastBuild/)-->

**Discover, assess and mitigate known vulnerabilities in your Java and Python projects.**

The open-source vulnerability assessment tool supports software development organizations in regards to the secure use of open-source components during application development. The tool analyzes **Java** and **Python** applications in order to
- detect whether they depend on open-source components with known vulnerabilities,
- collect evidence regarding the execution of vulnerable code in a given application context (through the combination of static and dynamic analysis techniques), and
- support developers in the mitigation of such dependencies.

As such, it addresses the OWASP Top 10 security risk A9, [Using Components with Known Vulnerabilities](https://www.owasp.org/index.php/Top_10-2017_A9-Using_Components_with_Known_Vulnerabilities), which is often the root cause of data breaches [[1]](https://snyk.io/blog/owasp-top-10-breaches/).

In comparison to other tools, the detection is code-centric and usage-based, which allows for more accurate detection and assessment than tools relying on meta-data.  It is a collection of client-side scan tools, RESTful microservices and rich [OpenUI5](https://openui5.hana.ondemand.com/) Web frontends.

## History

Originally developed by SAP Security Research [[2]](https://www.sap.com/documents/2017/08/f2895a6e-ca7c-0010-82c7-eda71af511fa.html)[[3]](https://scholar.google.com/citations?user=FOEVZyYAAAAJ&hl=en), the tool has become the officially recommended open-source scan tool for Java applications at SAP. Since the beginning of 2017, it has been used to perform 20K+ scans of more than 600+ Java development projects.

The tool approach is best described in the following scientific papers, please cite if you use the tool for scientific works/papers:
- Serena Ponta, Henrik Plate, Antonino Sabetta, [Beyond Metadata: Code-centric and Usage-based Analysis of Known Vulnerabilities in Open-source Software](https://arxiv.org/abs/1806.05893), 34th International Conference on Software Maintenance and Evolution (ICSME), 2018
- Henrik Plate, Serena Ponta, Antonino Sabetta, [Impact Assessment for Vulnerabilities in Open-Source Software Libraries](https://arxiv.org/pdf/1504.04971.pdf), 31st International Conference on Software Maintenance and Evolution (ICSME), 2015

## Features

In the following a couple of example features:
- **Detection of vulnerable code** is realized by discovering method signatures in Java archives and comparing their source and byte code with the vulnerable and fixed version (as known from the fix commit). As such, the detection is more acurate than for approaches based on meta-data (less false-positives and false-negatives). In particular, it is robust against rebundling, a very common practice in the Java ecosystem.
- **Assessment of vulnerable dependencies** by application developers and security experts is supported by information about the potential and actual execution of vulnerable code. This information is based on call graph analysis and trace information collected during JUnit and integration tests. Going down to the granularity of single methods, application developers are presented with the potential and actual call stack from application code till vulnerable code.
- The addition of new vulnerabilities to the knowledge base does not require the re-scan of applications. In other words, right after  an addition to the knowledge base, it is immediately known whether previously scanned applications are affected or not.
- **Mitigation proposals** consider the _reachable_ share of dependencies, i.e., the set of methods that can be potentially reached from application code union the actual executions observed during tests. This information is used to compute several metrics aiming to let developers chose the best non-vulnerable replacement of a vulnerable dependency (best in regards to non-breaking and with least regression likelihood).
- Individual findings can be exempted if developers come to the conclusion that a vulnerability cannot be exploited in a given application-context. This information can be maintained in an auditable fashion (incl. timestamp and author information) and typically prevents build exceptions during CI/CD pipelines.
- Organization-internal CERTs can query for all applications affected by a given vulnerability. This feature supports, for instance, larger development organizations with many software applications developed by distributed and de-central development units.

Visit the Wiki in order to get a better understanding of the various features and the Web frontends.

## Requirements

The open-source vulnerability assessment tool has a distributed architecture composed of a couple of Spring Boot microservices, two Web frontends and a number of client-side scanners/plugins, which perform the actual analysis of application and dependency code on build systems or developer workstations.

<p align="center"><img src="./docs/media/images/components-2.png" height="200"/></p>

You need **[Docker](https://www.docker.com/)**
- to build all artifacts and
- operate the various server-side components.

You need the **[Java 8 JRE](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)** and the following to run the various client-side scanners/plugins:
- **[Maven 3.3+](https://maven.apache.org/download.cgi)** for the analysis of Maven projects using the `plugin-maven`,
- **[Python 3](https://www.python.org/downloads/)** as well as the packages **pip, virtualenv and setuptools** (`pip install <name>`) for the analysis of Python applications using the `cli-scanner`, and
- **[Gradle 4](https://gradle.org/install/)** for the analysis of Gradle projects using the `plugin-gradle`.

## Download and Installation: Try it out!

For your convenience, you can use Docker to build and run the open-source vulnerability assessment tool on your own
in a few easy steps. Detailed instructions are available [here](docker/README.md).

## Configuration

Even though the default configuration works out of the box, you may want to configure the various components to adapt the tool to your needs and organization. See below for a some examples and refer to the Wiki for detailed configuration instructions.

The client-side scan tools (`plugin-maven`, `plugin-gradle`, `cli-scanner`) can be configured to integrate and align with organization-specific development and security policies. For instance, it is configurable whether build exceptions are thrown as soon as dependencies with vulnerable code exist, or only if the execution of such vulnerable code is observed during JUnit or integration tests. 

The microservices and Web frontends can be configured to integrate with an organization-specific build and development infrastructure. For instance, private Nexus or PyPI repositories can be configured to provide information about non-public components.

The tool was originally built to manage publicly known vulnerabilities in open-source components, however, the knowledge base can also (in parallel) comprise non-public vulnerabilities. This possibility is particularly useful for larger development organiations with many internal re-use components. As for open-source components, thanks to the integration with private Nexus and PyPI repositories, the tool can suggest the latest non-vulnerable version of such internal re-use components.

## Limitations

**Important**: Due to the current lack of an authentication and authorization mechanism, it is NOT recommended to run the Web frontends and server-side microservices on systems accessible from the Internet.

Other limitations:
- Static and dynamic analyses are not implemented for Python
- Static analysis for Java is only supported until Java 8

## Known Issues

The list of current issues is available [here](https://github.com/SAP/vulnerability-assessment-tool/issues)

## How to obtain support

Use the following link to [Stack Overflow](https://stackoverflow.com/questions/tagged/vulas) to search for FAQs or to request help.

Bug reports shall be submitted as GitHub issues, please refer to the next section for more details.

## Contributing

Our aim is to build a lively community, hence, we welcome any exchange and collaboration with individuals and organizations interested in the use, support and extension of the open-source vulnerability assessment tool.

Please read [this document](CONTRIBUTING.md) to read more about your options:
 * [Help Others](CONTRIBUTING.md#help-others) on [Stack Overflow](https://stackoverflow.com/questions/tagged/vulas)
 * [Report Bugs](CONTRIBUTING.md#report-an-issue) as GitHub issues
 * [Analyze Bugs](CONTRIBUTING.md#analyze-issues)
 * [Contribute Code](CONTRIBUTING.md#contribute-code) (fixes and features)
 * [Contribute to the Vulnerability Knowledge Base](CONTRIBUTING.md#knowledge-base): The fuel driving the open-source vulnerability assessment tool is its vulnerability database. We plan to use a dedicated GitHub repository to organize the sharing and joint maintenance of information about publicly known vulnerabilities in open-source components. In the majority of the cases, such information essentially consists of a bug identifier and references to one or more commits (created by the developers of the vulnerable component in order to fix the vulnerability). 

## To-Do (upcoming changes)

The following is a subset of pending feature requests:
- Static and dynamic analysis for Python
- Support of JavaScript (client- and server-side)
- UI dashboards for workspaces

## License
Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.

This project is licensed under the Apache Software License, v.2 except as noted otherwise in the [LICENSE file](LICENSE.txt).

## References
\[1\] [https://snyk.io/blog/owasp-top-10-breaches/](https://snyk.io/blog/owasp-top-10-breaches/)

\[2\] [https://www.sap.com/documents/2017/08/f2895a6e-ca7c-0010-82c7-eda71af511fa.html](https://www.sap.com/documents/2017/08/f2895a6e-ca7c-0010-82c7-eda71af511fa.html)

\[3\] [https://scholar.google.com/citations?user=FOEVZyYAAAAJ&hl=en](https://scholar.google.com/citations?user=FOEVZyYAAAAJ&hl=en)
