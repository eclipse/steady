<img src="vulas_logo_va_2048.png" height="64" width="64"> **Code-centric and usage-based vulnerability assessment for Java and Python**

## Description

Vulas supports software development organizations in regards to the secure use of open-source components during application development. It is a collection of client-side scan tools, RESTful microservices and rich [OpenUI5](https://openui5.hana.ondemand.com/) Web frontends.

Vulas analyzes Java and Python applications in order to
- detect whether they depend on open-source components with known vulnerabilities,
- collect evidence regarding the execution of vulnerable code in a given application context (through the combination of static and dynamic analysis techniques), and
- support developers in the mitigation of such dependencies.

As such, it addresses the OWASP Top 10 security risk A9, [Using Components with Known Vulnerabilities](https://www.owasp.org/index.php/Top_10-2017_A9-Using_Components_with_Known_Vulnerabilities), which is often the root cause of data breaches.

In comparison to other tools, the detection is code-centric and usage-based, which allows for more accurate detection and assessment than tools relying on meta-data. The approach implemented in Vulas is best described in the following scientific papers:
- Serena Ponta, Henrik Plate, Antonino Sabetta, [Beyond Metadata: Code-centric and Usage-based Analysis of Known Vulnerabilities in Open-source Software](https://arxiv.org/abs/1806.05893), 34th International Conference on Software Maintenance and Evolution (ICSME), 2018
- Henrik Plate, Serena Ponta, Antonino Sabetta, [Impact Assessment for Vulnerabilities in Open-Source Software Libraries](https://arxiv.org/pdf/1504.04971.pdf), 31st International Conference on Software Maintenance and Evolution (ICSME), 2015

## Disclaimer

The 

## Requirements

### Slient-side tools (for Java and Python)

### Server-side REST services and several Web frontends

## Download and Installation

## Configuration

## Limitations


## Known Issues

## How to obtain support
--> Stackoverflow

## Contributing
You want to contribute? You're very welcome, that is why we open-sourced the tool!

Please read [this document](CONTRIBUTIONS.md) to read more about your options:
 * [Help Others](CONTRIBUTING.md#help-others)
 * [Analyze Issues](CONTRIBUTING.md#analyze-issues)
 * [Report an Issue](CONTRIBUTING.md#report-an-issue)
 * [Contribute Code](CONTRIBUTING.md#contribute-code)
 * [Contribute to the Vulnerability Knowledge Base](CONTRIBUTING.md#knowledge-base)

## To-Do (upcoming changes)
The following is a subset of pending feature requests:
- Static and dynamic analysis for Python
- Support of JavaScript (client- and server-side)
- UI dashboards for workspaces

## License
Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
This project is licensed under the Apache Software License, v. 2 except as noted otherwise in the [LICENSE file](LICENSE.txt).
