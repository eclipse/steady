# Vulnerability database

The vulnerability database contains detailed (code-level) information about publicly disclosed vulnerabilities affectint open source projects. This is *the fuel* of the code-centric analysis performed by **@@PROJECT_NAME@@**, which is only able to detect and assess vulnerabilities if they are present in this vulnerability database.

The fix commits for hundreds of vulnerabilities in Java and Python open source projects are available in a [dedicated GitHub repository (project "KB")](https://github.com/SAP/project-kb).

__Quick start__

: Here you can find the steps to populate your vulnerability database with five known vulnerabilities. Of course, you can exercise the tutorial for all the other vulnerabilities from [project "KB"](https://github.com/SAP/project-kb) as well.

: [Go to the Tutorials page](./tutorials/vuln_db_tutorial/)


__Contributing to the vulnerability database__

: Here you can find information about how to contribute information about old or new publicly disclosed vulnerabilities to [project "KB"](https://github.com/SAP/project-kb)

: [Go to the Contribute page](../contributor/#contribute-to-the-vulnerability-knowledge-base)


The code-level vulnerability analysis performed by @@PROJECT_NAME@@ is based on the so-called change list, which is a set of constructs (e.g., Java methods) changed to fix a given security vulnerability.

Vulnerability detection is based on construct containment: a library is affected by a vulnerability if it contains the constructs that were changed to fix a vulnerability.

Once a vulnerability's change list has been created and added to the database, every application analyzed in the past is automatically assessed with regard to the new vulnerability. In other words, application owners see immediately whether their application is affected or not, without the need to re-run the analysis.

A list of the vulnerabilities saved in the database of your installation can be obtained from `@@ADDRESS@@/backend/bugs`

Detailed information for a given vulnerability can be obtained using the following link `@@ADDRESS@@/backend/bugs/<foo>` (where `<vuln_id>` has to be replaced by a real vulnerability identifier).

[KB Importer](../user/manuals/kb_importer/)  is used to import vulnerabilities into the database.
