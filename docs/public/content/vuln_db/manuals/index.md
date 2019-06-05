# Vulnerability Database Manual

The code-level vulnerability analysis performed by @@PROJECT_NAME@@ is based on the so-called change list, which is a set of constructs (e.g., Java methods) changed to fix a given security vulnerability.

Vulnerability detection is based on construct containment: a library is affected by a vulnerability if it contains the constructs that were changed to fix a vulnerability.

Once a vulnerability's change list has been created and added to the database, every application analyzed in the past is automatically assessed with regard to the new vulnerability. In other words, application owners see immediately whether their application is affected or not, without the need to re-run the analysis.

An up-to-date list of all vulnerabilities currently comprised in the database can be obtained using the following link: `@@ADDRESS@@/backend/bugs`

Detailed information for a given vulnerability can be obtained using the following link, whereby `<foo>` has to be replaced by the vulnerability identifier:  `@@ADDRESS@@/backend/bugs/<foo>`

The [Patch Analyzer](../../vuln_db/manuals/patch_analyzer/) module can be used to add vulnerabilities to the knowledge base.

In the current implementation, the vulnerability detection is based on the containment of the fully-qualified name of a construct changed to fix a vulnerability in a library. The information whether the library contains the vulnerable or fixed version of the construct is computed asynchronously by the [Patch Lib Analyzer](../../vuln_db/manuals/patch_lib_analyzer/).
