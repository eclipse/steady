# Vulnerability Data

## How to import vulnerability data in the @@PROJECT_NAME@@ backend

**TL;DR: There is nothing to do, it's all automated :-)**

When you deploy @@PROJECT_NAME@@ using Docker (using the script `setup-steady.sh`), not only the vulnerability data from project KB is *automatically imported*,
but it is also *periodically updated* so that any new vulnerabilities are imported automatically into your @@PROJECT_NAME@@ backend.

If you want to customize the mechanism whereby vulnerabilities are imported or if you just want to learn more about it, please refer to the dedicated [technical documentation](../user/manuals/updating_vuln_data).

## Why code-level vulnerability data are needed

The code-level vulnerability analysis performed by @@PROJECT_NAME@@ is based on the concept of *change list*, a set of constructs (e.g., methods) that are
changed to fix a given security vulnerability. The vulnerability detection capabilities of @@PROJECT_NAME@@ rely on this concept: an artifact (e.g. a library) is considered to be affected by a vulnerability if it contains the constructs that were changed to fix a vulnerability.

A positive consequence of this approach is that, once the change list for a vulnerability has been created and added to the database, one can immediately
determine if any of the applications scanned in the past are potentially impacted by this new vulnerability, no need to re-run the analysis for each application!

Hence, if one considers **@@PROJECT_NAME@@** as a powerful machine, then vulnerability data are *the fuel*, since **@@PROJECT_NAME@@** can only detect and assess vulnerabilities if they are present in its vulnerability database.

The fix commits for hundreds of vulnerabilities that affect Java and Python open source projects are mantained in a dedicated repository ([project "KB"](https://github.com/SAP/project-kb)) that focuses on fostering a community-based approach to gathering and maintaining a comprehensive knowledge base.

@@PROJECT_NAME@@ automatically imports data from project KB, so in typical scenarios you do not need to do anything for it to work.

## Contributing to the vulnerability database

[In this page](https://sap.github.io/project-kb/contributing/) you will find information about how to contribute vulnerability information to project KB.


## How to list the vulnerabilities that are currently available in your instance of @@PROJECT_NAME@@

Assuming that @@PROJECT_NAME@@ is deployed on `@@ADDRESS@@`, you can list of the vulnerabilities saved in the database of your installation via this
endpoint: [@@ADDRESS@@/backend/bugs](@@ADDRESS@@/backend/bugs)

Detailed information for a given vulnerability can be obtained using the following link `@@ADDRESS@@/backend/bugs/<foo>` (where `<vuln_id>` has to be replaced by a real vulnerability identifier).
