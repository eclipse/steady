<img src="vulas_logo_va_2048.png" height="64" width="64">

## Description

Vulas supports software development organizations in regards to the secure use of open-source components during application development. It is a collection of client-side scan tools, RESTful microservices and rich [OpenUI5](https://openui5.hana.ondemand.com/) Web frontends.

Vulas analyzes **Java** and **Python** applications in order to
- detect whether they depend on open-source components with known vulnerabilities,
- collect evidence regarding the execution of vulnerable code in a given application context (through the combination of static and dynamic analysis techniques), and
- support developers in the mitigation of such dependencies.

As such, it addresses the OWASP Top 10 security risk A9, [Using Components with Known Vulnerabilities](https://www.owasp.org/index.php/Top_10-2017_A9-Using_Components_with_Known_Vulnerabilities), which is often the root cause of data breaches [[1]](https://snyk.io/blog/owasp-top-10-breaches/).

In comparison to other tools, the detection is code-centric and usage-based, which allows for more accurate detection and assessment than tools relying on meta-data. The approach implemented in Vulas is best described in the following scientific papers:
- Serena Ponta, Henrik Plate, Antonino Sabetta, [Beyond Metadata: Code-centric and Usage-based Analysis of Known Vulnerabilities in Open-source Software](https://arxiv.org/abs/1806.05893), 34th International Conference on Software Maintenance and Evolution (ICSME), 2018
- Henrik Plate, Serena Ponta, Antonino Sabetta, [Impact Assessment for Vulnerabilities in Open-Source Software Libraries](https://arxiv.org/pdf/1504.04971.pdf), 31st International Conference on Software Maintenance and Evolution (ICSME), 2015

## History

Vulas has been originally developed by SAP Security Research [[2]](https://www.sap.com/documents/2017/08/f2895a6e-ca7c-0010-82c7-eda71af511fa.html)[[3]](https://scholar.google.com/citations?user=FOEVZyYAAAAJ&hl=en). Since then, it has become the officially recommended open-source scan tool for Java application at SAP. Since the beginning of 2017, it has been used to perform 20K+ scans of more than 600+ Java development projects.

## Features

In the following a couple of example features:
- The detection of vulnerable code is realized by discovering method signatures in Java archives and comparing their source and byte code with the vulnerable and fixed version (as known from the fix commit). As such, the detection is more acurate than for approaches based on meta-data (less false-positives and false-negatives). In particular, it is robust against rebundling, a very common practice in the Java ecosystem.
- The assessment of vulnerable dependenciesby by application developers and security experts is supported by collecting information about the potential and actual execution of vulnerable code. This information is based on call graph analysis and trace information collected during JUnit and integration tests. Going down to the granularity of single methods, application developers are presented with the potential and actual call stack from application code till vulnerable code.
- The addition of new vulnerabilities to the knowledge base does not require the re-scan of applications.
- Mitigation proposals consider the _reachable_ share of dependencies, i.e., the set of methods that can be potentially reached from application code union the actual executions observed during tests. This information is used to compute several metrics aiming to let developers chose the best non-vulnerable replacement of a vulnerable dependency (best in regards to non-breaking and with least regression likelihood).
- Individual findings can be exempted if developers come to the conclusion that a vulnerability cannot be exploited in a given application-context. This information can be maintained in an auditable fashion (incl. timestamp and author information) and typically prevents build exeptions during CI/CD pipelines.
- Organization-internal CERTs can query for all applications affected by a given vulnerability. This feature supports, for instance, larger development organizations with many software applications developed by distributed and de-central development units.

## Requirements

For the time being, Vulas primarily addresses software development organizations. The main reason is that its setup and operation requires some investment (see below). However, the planned release of additional artifacts will lower this hurdle and foster the adoption of Vulas. To put it in other words: Vulas is not (yet) a tool that can be downloaded and run out-of-the-box in 5 minutes.

## Download and Installation

Vulas is a collection of client-side scan tools, two RESTful microservices and two rich [OpenUI5](https://openui5.hana.ondemand.com/) Web frontends.

Before being able to actually scan applications using the client-side tools, one has to:
1. Clone this repository, and build the various binaries using `mvn clean install`
2. Deploy the two microservices (`rest-backend` and `rest-lib-utils`)
3. Deploy the two Web frontends (`frontend-apps` and `frontend-bugs`)
4. Add vulnerabilities to the Vulas knowledge base (using the `patch-analyzer`, which analyzes the commits fixing given vulnerabilities in open-source components, and the `patch-lib-analyzer`, which checks whether libraries are affected by vulnerabilities)

Obviously, those activities are relatively involved, hence, we aim at releasing the following additional artifacts in order to facilitate the setup and operation of Vulas. The current plan is to release by Q4 2018, in dedicated repositories:
- Docker and Docker Compose files to facilitate the operation (steps 2 and 3)
- The existing knowledge base comprising 700+ vulnerabilities (step 4) 

Moreover, in order to avoid the necessity to build the various binaries, we aim at publishing (some of) them on public repositories such as Maven Central. 

## Configuration

The client-side scan tools (`plugin-maven`, `plugin-gradle`, `cli-scanner`) can be configured to integrate and align with organization-specific development and security policies. For instance, it is configurable whether build exceptions are thrown as soon as dependencies with vulnerable code exist, or only if the execution of such vulnerable code is observed during JUnit or integration tests. 

The microservices and Web frontends can be configured to integrate with an organization-specific build and development infrastructure. For instance, private Nexus or PyPI repositories can be configured to provide information about non-public components.

Vulas was originally built to manage publicly known vulnerabilities in open-source components, however, the knowledge base can also (in parallel) comprise non-public vulnerabilities. This possibility is particularly useful for larger development organiations with many internal re-use components. As for open-source components, thanks to the integration with private Nexus and PyPI repositories, Vulas can suggest the latest non-vulnerable version of such internal re-use components.

<!--

## Limitations

As of today, the static and dynamic analysis is only available for Java applications.

## Known Issues

Lack of authentication and authorization.

-->

## How to obtain support

We will create a dedicated tag on [Stack Overflow](https://stackoverflow.com) to facilitate the support of and discussion among Vulas users.

## Contributing

Our aim is to build a lively community, hence, we welcome any exchange and collaboration with individuals and organizations interested in the use, support and extension of Vulas.

You want to contribute? Here are the options we currently have in mind:
- Help others on Stack Overflow
- Report bugs as GitHub issues
- Analyze GitHub issues
- Contribute code (bug fixes and features)
- Contribute vulnerability information to the knowledge base: As mentioned before, we plan to use a dedicated GitHub repository to organize the sharing and joint maintenance of information about publicly known vulnerabilities in open-source components. In the majority of the cases, such information essentially consists of a bug identifier and references to one or more commits (created by the developers of the vulnerable component in order to fix the vulnerability). 

<!--

## To-Do (upcoming changes)
The following is a subset of pending feature requests:
- Static and dynamic analysis for Python
- Support of JavaScript (client- and server-side)
- UI dashboards for workspaces

-->

## License
Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
This project is licensed under the Apache Software License, v. 2 except as noted otherwise in the [LICENSE file](LICENSE.txt).

## References
- [1] [https://snyk.io/blog/owasp-top-10-breaches/](https://snyk.io/blog/owasp-top-10-breaches/)
- [2] [https://www.sap.com/documents/2017/08/f2895a6e-ca7c-0010-82c7-eda71af511fa.html](https://www.sap.com/documents/2017/08/f2895a6e-ca7c-0010-82c7-eda71af511fa.html)
- [3] [https://scholar.google.com/citations?user=FOEVZyYAAAAJ&hl=en](https://scholar.google.com/citations?user=FOEVZyYAAAAJ&hl=en)
