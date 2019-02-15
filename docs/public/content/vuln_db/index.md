# Vulnerability database

The vulnerability database comprises code-level information about publicly disclosed vulnerabilities in open source projects, which is the fuel of the code-centric analysis performed by **@@PROJECT_NAME@@**. The tool is able to detect and assess all and only the vulnerabilities present in this PostgreSQL database.

The code-level information is gathered by analyzing the commits that the open source developers submitted in order to fix the respective vulnerability (the so-called fix commit). The results of this analysis comprise, for instance, the unique names and abstract syntax trees of all methods or functions modified in the respective fix-commits.

The fix commits for hundreds of vulnerabilities in Java and Python open source projects are available in a dedicated GitHub repository called [vulnerability assessment knowledge base](https://github.com/SAP/vulnerability-assessment-kb).

__Step by step tutorial__

: Here you can find the steps to populate your vulnerability database with five known vulnerabilities. Of course, you can exercise the tutorial for all the other vulnerabilities of the [vulnerability assessment knowledge base](https://github.com/SAP/vulnerability-assessment-kb) as well.

: [Go to the Tutorials page](./tutorials/vuln_db_tutorial)

__Manual__

: Here you can find a description of the main activities and tools around the vulnerability database.

: [Go to the Vulnerability Database Manual](./manuals)

__Contribute__

: Here you can find information about how to contribute new publicly disclosed vulnerabilities to the knowledge base

: [Go to the Contribute page](../contributor/#contribute-to-the-vulnerability-knowledge-base)

!!! warning "Orange Hourglass"
	Whenever an application library contains the signature of a construct that was changed to fix a vulnerability, but the patch lib analyzer didn't yet (or could not) establish whether it contains the vulnerable or fixed version of the construct, then the tool reports the vulnerability in the web frontend with an ORANGE hourglass in the column "Inclusion of vulnerable code".

	To resolve orange hourglasses it's necessary to feed the tool with the information whether the library contains the vulnerable or fixed version of the construct changed in the vulnerability fix. This can be done either [manually](./manuals/manual_assessment) or automatically with the [patch-lib-analyzer](./manuals/patch_lib_analyzer) (for a subset of cases).
