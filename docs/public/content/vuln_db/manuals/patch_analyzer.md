# Patch Analyzer

The vulnerability database contains known vulnerabilities for which a fix (aka patch) exists. In fact, the fix (commit fix in the vulnerable library project) of the vulnerability must be provided in order to add a vulnerability to the database.

Each vulnerability in the database is characterized by the set of program constructs that were changed in order to fix it.

!!! info "Program Construct"
	A program construct (or simply construct) is a structural element of the source code characterized by a type (e.g., `package`, `class`, `constructor`, `method`), a language (e.g., Java, Python), and a unique identifier (e.g., the fully-qualified name).

!!! info "Construct Change"
	A construct change is characterized by a change operation (i.e., addition, deletion or modification) on a construct. In case of modifications the vulnerability database also contains the abstract syntax tree (AST) of the construct before and after the fix, i.e., the AST of the vulnerable and fixed construct.

The analysis can be done using the `patch-analyzer` module.

## Configuration

To add a new vulnerability fixed in a given library repository, the following fields need to be provided as input:

* `bug (-b) <arg>` : vulnerability identifier
* `repo (-r) <arg>` : URL of the VCS repository hosting the library project
* `revision (-e) <arg>` :  One or multiple revisions (multiple ones must be comma-separated w/o blanks). In the case of Git repositories, the revision can be optionally concatenated with ':' with the branch information.
* `description (-descr) <arg>` : Textual vulnerability description (optional, it must be provided for vulnerabilities not available from the NVD)
* `links (-links) <arg>` : Comma-separated list of links to comprehensive vulnerability information (optional, it must be provided for vulnerabilities not available from the NVD)
* `skip-if-existing (-sie)` : Skips the analysis of a vulnerability if it already exists in the backend
* `upload (-u)` : Upload construct changes

Additionally the @@PROJECT_NAME@@ backend service URL must be configured (see [here](../../../admin/) for instructions).

!!! Info
	In case you want to be sure about the analysis result before uploading it to the backend, you can run the analysis without the -u option so that the resulting JSON will be saved to file for you to review (e.g., to check that the list of construct changes is not empty).

The options -descr and -links can be used to add a custom description and/or link url. This is of utmost importance in case the vulnerability is not available in the NVD. Such information can also be provided from the bugs frontend after the bug has been created (both fields must be provided when saving).

!!! Critical
	If a vulnerability fix is applied in multiple repositories, it must be analysed as two separate vulnerabilities with different identifiers.

## Vulnerabilities without change list

In case a vulnerability fix does not include any code change (e.g., only changes configuration parameters), it is still possible to add it to the vulnerability database  by manually creating and POSTing the initial JSON. Also the initial affected versions have to be POSTed as JSON and then assessed via the bug frontend, see [Manual Assessments] (../../vuln_db/manual_assessment).

The first POST request creates the entry into the vulnerabilities database (in the following example we create and entry for CVE "S2-043"):

`POST http://@@ADDRESS@@:/backend/bugs`

```json
{
"bugId": "S2-043",
"constructChanges": [],
"createdBy": "sp",
"description": "Usage of the Config Browser in a production environment can lead to exposing vunerable information of the application.",
"source": "StrutsSecBulletin",
"reference": ["https://struts.apache.org/docs/s2-043.htm"]
}
```

The second POST request defines which versions to be marked as affected or not. Please notice that the id of the CVE to update ("S2-403" in the following example) has to be used/replaced in the following POST request.

`POST http://@@ADDRESS@@:/backend/bugs/S2-043/affectedLibIds?source=MANUAL`

```json
[
{
"libraryId":{ "artifact": "struts2-core", "version": "2.0.11", "group": "org.apache.struts" },

"lib": null,
"affectedcc": [],
"source": "MANUAL",
"affected": true
}
]
```

## Vulnerabilities of proprietary libraries

The vulnerability Database can also handle undisclosed vulnerabilities and proprietary components. This allows to make company-internal users of proprietary libraries aware and update to non-vulnerable versions.

The required elements for the creation are:

- Brief description of the vulnerability (max 2-3 lines) using the NVD vulnerability description style and phrasing (e.g. including the type of vulnerability, the affected software component).
- URL of the code repository of the affected component.
- Commit(s) used to the fix the vulnerability in the component.
- Links with additional information about the vulnerability (if available).
