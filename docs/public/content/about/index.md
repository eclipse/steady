# About

@@PROJECT_NAME@@ analyzes Java and Python applications in order to:

- **detect** whether they depend on open-source components with known vulnerabilities,
- **collect evidence** of the execution of vulnerable code in a given application context (through a novel combination of static and dynamic analysis), and
- support developers in the **mitigation** of such vulnerable dependencies.

@@PROJECT_NAME@@ addresses the OWASP Top 10 security risk A9, [Using Components with Known Vulnerabilities](https://owasp.org/www-project-top-ten/OWASP_Top_Ten_2017/Top_10-2017_A9-Using_Components_with_Known_Vulnerabilities).
Differently from other tools that have similar goals, the detection approach of @@PROJECT_NAME@@ is _code-centric and usage-based_,
which allows for a more accurate detection and assessment than tools relying on meta-data.

@@PROJECT_NAME@@ is implemented as a collection of client-side tools (for Java and Python), server-side RESTful services and several Web frontends. Initially developed by [SAP Security Research](https://www.sap.com/documents/2017/08/f2895a6e-ca7c-0010-82c7-eda71af511fa.html), @@PROJECT_NAME@@ was adopted internally by SAP as early as 2015.
The tool has been open-sourced in October 2018 under the Apache License v.2.0.

The approach implemented in @@PROJECT_NAME@@ is described in detail in the the following scientific papers:

- Serena Ponta, Henrik Plate, Antonino Sabetta, [Beyond Metadata: Code-centric and Usage-based Analysis of Known Vulnerabilities in Open-source Software](https://arxiv.org/abs/1806.05893), 34th International Conference on Software Maintenance and Evolution (ICSME), 2018 (recipient of the IEEE TCSE Distinguished Paper Award)
- Henrik Plate, Serena Ponta, Antonino Sabetta, [Impact Assessment for Vulnerabilities in Open-Source Software Libraries](https://arxiv.org/pdf/1504.04971.pdf), 31st International Conference on Software Maintenance and Evolution (ICSME), 2015

## The Team

- Henrik Plate (SAP Security Research)
- Serena E. Ponta (SAP Security Research)
- Antonino Sabetta (SAP Security Research)
- Cédric Dangremont (SAP Security Testing and Validation)
- Alessandro Pezzé (SAP Security Testing and Validation)
