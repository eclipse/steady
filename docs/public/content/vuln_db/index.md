# Vulnerability database

The vulnerability database is the knowledge base of the vulnerability assessment tool. The tool is able to detect and assess all and only the vulnerabilities present in the knowledge base.

__Step by step tutorial__

: Here you can find the steps to populate your vulnerability database with five known vulnerabilities.

: [Go to the Tutorials page](./tutorials/vuln_db_tutorial)

__Manual__

: Here you can find a description of the main activities and tool around the vulnerability database.

: [Go to the Vulnerability Database Manual](./tutorials/vuln_db_tutorial)

__Contribute to the vulnerability database__

: Here you can find information about how to contribute new known vulnerabilities

: [Go to the Contribute page](../../../contributor/#contribute-to-the-vulnerability-knowledge-base)

!!! warning "Orange Hourglass"
	Whenever an application library contains the signature of a construct that was changed to fix a vulnerability, but the patch lib analyzer didn't yet (or could not) establish whether it contains the vulnerable or fixed version of the construct, then the tool reports the vulnerability in the web frontend with an ORANGE hourglass in the column "Inclusion of vulnerable code".

	To resolve orange hourglasses it's necessary to feed the tool with the information whether the library contains the vulnerable or fixed version of the construct changed in the vulnerability fix. This can be done either [manually](./manuals/manual_assessment) or automatically with the [patch-lib-analyzer](./manuals/patch_lib_analyzer) (for a subset of cases).
