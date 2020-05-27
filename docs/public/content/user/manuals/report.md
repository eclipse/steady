# Report Manual

## Introduction

Result reports are generated through the execution of the `report` goal, more information on how to run and configure the goal can be found [here](../analysis/#create-result-report-report). They are generated in three different formats (JSON, XML and HTML), but the remainder of this page will focus on the HTML report.

For Maven reactor projects, a big advantage of reports is that they aggregate the findings for all modules.

## Report Header

The report header contains general information about the scanned application or Maven modules.

<center class='expandable'>
    [![start_page](./img/report_header.png)](./img/report_header.png)
</center>

It provides:

- Date and time of the report creation.
- Information if the report is an aggregated report or not (incl. the list of considered modules).
- The conclusion of the `report` goal, displayed in red if vulnerabilities were found and an exception is thrown, green otherwise.
- Important configuration settings used when running the `report` goal (exemptions and exception threshold).

## Report Body

The report body contains two lists of findings: Those below the header "Vulnerabilities" are relevant findings resulting in a build exception, those below the header "Exempted Vulnerabilities" have been exempted according to the configuration settings mentioned in the report header.

<center class='expandable'>
    [![start_page](./img/report_core.png)](./img/report_core.png)
</center>

The color code on the left of each item reflects the CVSS score of the respective vulnerability, it is grey in case no CVSS score is present.

The table below each item contains a subset of those applications/modules having a dependency on the respective vulnerable archive.

You can reach the Web frontend of each application/project by clicking on its name.

Moreover, each application/module comes with a tooltip showing the complete GAV coordindates, the nature of its dependency on the respective archive (scope and transitivity), and the exemption reason (in case of exempted vulnerabilities).

The three columns indicate whether the vulnerable code is present, whether it is potentially executable or actually executed.
