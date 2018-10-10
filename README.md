<p align="center"><img height="64" src="./docs/media/images/logo/vulas.png"></p>

# Open-source vulnerability assessment tool [![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.wdf.sap.corp/vulas/vulas/blob/master/LICENSE.txt) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md) <!-- [![Build Status](https://<host>/job/<name>/job/<job-name>/lastBuild/badge/icon)](https://<host>/job/<name>/job/<job-name>/lastBuild/)-->

> Discover, assess and mitigate known vulnerabilities in your Java and Python projects

The open-source vulnerability assessment tool supports software development organizations in regards to the secure use of open-source components during application development. It is a collection of client-side scan tools, RESTful microservices and rich [OpenUI5](https://openui5.hana.ondemand.com/) Web frontends.

The tool analyzes **Java** and **Python** applications in order to
- detect whether they depend on open-source components with known vulnerabilities,
- collect evidence regarding the execution of vulnerable code in a given application context (through the combination of static and dynamic analysis techniques), and
- support developers in the mitigation of such dependencies.

As such, it addresses the OWASP Top 10 security risk A9, [Using Components with Known Vulnerabilities](https://www.owasp.org/index.php/Top_10-2017_A9-Using_Components_with_Known_Vulnerabilities), which is often the root cause of data breaches [[1]](https://snyk.io/blog/owasp-top-10-breaches/).

In comparison to other tools, the detection is code-centric and usage-based, which allows for more accurate detection and assessment than tools relying on meta-data. The approach implemented in the open-source vulnerability assessment tool as is best described in the following scientific papers:
- Serena Ponta, Henrik Plate, Antonino Sabetta, [Beyond Metadata: Code-centric and Usage-based Analysis of Known Vulnerabilities in Open-source Software](https://arxiv.org/abs/1806.05893), 34th International Conference on Software Maintenance and Evolution (ICSME), 2018
- Henrik Plate, Serena Ponta, Antonino Sabetta, [Impact Assessment for Vulnerabilities in Open-Source Software Libraries](https://arxiv.org/pdf/1504.04971.pdf), 31st International Conference on Software Maintenance and Evolution (ICSME), 2015

## History

Originally developed by SAP Security Research [[2]](https://www.sap.com/documents/2017/08/f2895a6e-ca7c-0010-82c7-eda71af511fa.html)[[3]](https://scholar.google.com/citations?user=FOEVZyYAAAAJ&hl=en), the tool has become the officially recommended open-source scan tool for Java applications at SAP. Since the beginning of 2017, it has been used to perform 20K+ scans of more than 600+ Java development projects.

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

To facilitate experiments and tests, we provide a set of Docker files to build and run the ensemble of services on a single machine (cf. section [Download and Installation](#download-and-installation)).

However, a setup on a single machine does not meet the requirements for productive use in regards to scalability and availability. In consequence, the tool primarily addresses software development organizations (with multiple development teams), which have the required resources to setup and operate the tool 24/7.

In order to proceed with the installation, you need to have [Docker](https://www.docker.com/) installed.

## Download and Installation

The installation comprises the following three steps. Once the installation is completed, you can start analyzing your Java and Python applications using the various client-side tools as described in the [Wiki](vulas/wiki).

1. Create all artifacts (Java archives)
    1. Clone the repository, open a command line and change to its root directory `<vulas-root>`
    2. Copy `<vulas-root>/docker/.env.sample` to `<vulas-root>/docker/.env` and specify all relevant settings
    3. Create the Docker image with `docker build --tag vulas-build-img -f <vulas-root>/docker/Dockerfile --build-arg http_proxy=<proxy> --build-arg https_proxy=<proxy> .` (omit the proxy arguments in case you're not running behind a proxy)
    4. Run the image with `docker run -it --rm -v $(pwd):/vulas --env-file <vulas-root>/docker/.env vulas-build-img`
    5. Copy the following JARs into the corresponding subdirectories of `<vulas-root>/docker`
        * `frontend-apps-<version>.jar`
        * `frontend-bugs-<version>.jar`
        * `rest-backend-<version>.jar`
        * `rest-lib-utils-<version>.jar`
        * `patch-lib-analyzer-<version>.jar`
    6. Make all Java artifacts available to your developers using your organization's package repositories, e.g., Nexus or artifactory.
2. Create and execute Docker images (on a single system)
    1. Build and run the images: `(cd docker && docker-compose up --build)`
3. Populate the vulnerability database
    1. Run the `PatchAnalyzer` for a number of sample vulnerabilities

**Important**: The single-system setup is not recommended for productive use in larger development organizations. To support 24/7 operation, it is recommended to run the Docker images built in step 2 in a private cloud or similar.

**Notes**:
- The build of the Gradle plugin and the Soot call graph analysis service requires the following changes prior to step 2.ii:
    - Add `-P gradle,soot` to the Maven call in the last line of `<vulas-root>/docker/Dockerfile`
    - Update the Maven configuration file `settings.xml` to make sure that the `soot` and `google` repositories will not be mirrored (see profiles `soot` and `gradle` in `pom.xml` for more information)

## Configuration

The client-side scan tools (`plugin-maven`, `plugin-gradle`, `cli-scanner`) can be configured to integrate and align with organization-specific development and security policies. For instance, it is configurable whether build exceptions are thrown as soon as dependencies with vulnerable code exist, or only if the execution of such vulnerable code is observed during JUnit or integration tests. 

The microservices and Web frontends can be configured to integrate with an organization-specific build and development infrastructure. For instance, private Nexus or PyPI repositories can be configured to provide information about non-public components.

The tool was originally built to manage publicly known vulnerabilities in open-source components, however, the knowledge base can also (in parallel) comprise non-public vulnerabilities. This possibility is particularly useful for larger development organiations with many internal re-use components. As for open-source components, thanks to the integration with private Nexus and PyPI repositories, the tool can suggest the latest non-vulnerable version of such internal re-use components.

## Limitations

**Important**: Due to the current lack of an authentication and authorization mechanism, it is NOT recommended to run the Web frontends and server-side microservices on systems accessible from the Internet.

Other limitations:
- Static and dynamic analyses are not implemented for Python
- Static analysis for Java is only supported until Java 8

## Known Issues

None.

## How to obtain support

We will soon create a dedicated tag on [Stack Overflow](https://stackoverflow.com) to facilitate the support of and discussion among users.

## Contributing

Our aim is to build a lively community, hence, we welcome any exchange and collaboration with individuals and organizations interested in the use, support and extension of the open-source vulnerability assessment tool.

Please read [this document](CONTRIBUTING.md) to read more about your options:
 * [Help Others](CONTRIBUTING.md#help-others) on Stack Overflow
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
