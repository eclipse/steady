# Library Assessment

The notion of "library assessment" refers to the process that establishes whether a library contains a construct modified to fix a vulnerability (aka changed-construct) in its vulnerable or fixed version. @@PROJECT_NAME@@ performs this process automatically, but in some cases manual intervention
is required (or desirable, e.g., to override the automated assessment).



## Automated Library Assessment

The process of library assessment is automated in @@PROJECT_NAME@@ through `patch library analyzer`, a Java application that
compares the abstract syntax tree (AST) of the body of the changed-construct contained in a library with the AST of its vulnerable and fixed version.

Unfortunately, vulnerability fixes are applied on source code whereas library releases imported within applications are binaries and
this represents a significant technical challenge. The `patch lib analyzer` addresses it by retrieving the source code of binaries (if available) from Maven repositories (e.g. Maven Central or organization's specific Nexus repositories).

!!! warning
	The current implementation only assesses libraries having a GAV (*group-artifact-version* identifier) known to Maven Central or configured Nexus repositories. The assessment of python artifacts available in PyPI is not supported at this time.

If the sources of a given library are available, the `patch lib analyzer` compares the ASTs of the changed-constructs with the AST of their vulnerable and fixed version. Once equalities are found in source code, they are also used to conclude--wherever possible--for cases where the source code is not available or equalities are not found. The `patch lib analyzer` may conclude that a library is fixed or vulnerable based on the following criteria:

- **AST EQUALITY**
	- vulnerable: if the abstract syntax tree (AST) of at least one construct of the library, is equal to the AST of a construct before it was modified to fix the vulnerability (and none is equal to the fixed version)
	- fixed: if the abstract syntax tree (AST) of at least one construct of the library, is equal to the AST of a construct after it was modified to fix the vulnerability (and none is equal to the vulnerable version)
- **MINOR EQUALITY** : The library version is minor of another one in the same minor release (i.e., both starting with x.y.) which has an AST equality to vulnerable
- **MAJOR EQUALITY**: The library version is major of another one in the same minor release (i.e., both starting with x.y.) which has an AST equality to fixed
- **INTERSECTION**
	- vulnerable: if the abstract syntax tree (AST) of all construct of the library, are "closer" (i.e., requires a smaller amount of changes) to the AST of a construct before it was modified to fix the vulnerability  than to the one after the change
	- fixed: if the abstract syntax tree (AST) of all construct of the library, are "closer" (i.e., requires a smaller amount of changes) to the AST of a construct after it was modified to fix the vulnerability than to the one before the change
- **GREATER RELEASE**: the library is in a minor release which was released after the most recent fix of the vulnerability. E.g., 3.4.0 release on Jan 2017 is a greater release for bug CVE-2016-1234 if its latest fix was applied on release 3.3.y before 2016, Dec 31st.

Even in cases where the automated approach cannot conclude, the data computed over the libraries are stored in the @@PROJECT_NAME@@ backend under the source **TO_REVIEW**.

The key data used to assess the libraries and always available for review are:

- *sourceAvailable*: whether the sources for the library are available in external repositories
- *construct in jar*: whether  a construct changed to fix a vulnerability is part of the library archive (Jar)
- *dTv* (distance to vulnerable): the number of changes to be applied to the AST of a construct in the library, to become equal to the AST of the construct before the fix (computer with ChangeDistiller)
- *dTf* (distance to fixed): the number of changes to be applied to the AST of a construct in the library, to become equal to the AST of the construct after the fix (computer with ChangeDistiller)

The results are visible in the column "Patch eval" of the bugs frontend. Clicking on the cell, all the information computed (based on which the assessment result was taken - if any) are available.

## Usage

The `patch lib analyzer` can be run as a periodic job or a single run. Configuration:

- `j (-jib)` : Run patch Eval as cron job (optional)
- `h (-hour) <arg>` : Delay for starting the job (hours) (optional, only relevant if -job is specified, Default: 0 )
- `p (-period) <arg>` : The period between successive executions (in hours) (optional, only relevant if -job is specified, Default: 6)
- `bug (-bug)` : Comma separated list of bugs to analyze (optional, all bugs analyzed if config not provided)
- `f (-toFile)` : Save JSON results to file; otherwise upload to backend (optional, default: false)
- `o (-overrideResults)` : Delete all existing results before upload; otherwise only upload results for AffectedLibraries not already existing in the backend (optional, default: false)

Other useful/required configurations:

- vulas.shared.cia.serviceUrl : url of the deployed `rest-lib-util` service (Mandatory)
- vulas.shared.backend.serviceUrl : Url of the deployed `rest-backend` service (Mandatory)
- vulas.patchEval.uploadResults : whether to upload the results to vulas.shared.backend.serviceUrl or save the JSON to file (equivalent to `-file`)
- vulas.patchEval.onlyAddNewResults : only upload results for AffectedLibraries not already existing in the backend; otherwise all existing results are deleted before the upload (equivalent to `-overrideResults`)
- vulas.patchEval.basefolder : filesystem path where to save and look for the csv files with the computed data per library
- vulas.patchEval.bugId : comma separated list of bug identifiers to analyze (equivalent to `-bug`)

Run as cron job

```sh
java -Dvulas.patchEval.uploadResults=true -Dvulas.patchEval.onlyAddNewResults=true -Dvulas.patchEval.basefolder=<csv_folder> -j -p 6 -Dvulas.shared.cia.serviceUrl=@@ADDRESS@@/cia -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/-Xmx6G -Xms6G -jar patch-lib-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar
```

Run for all bugs

```sh
java -Dvulas.patchEval.uploadResults=true -Dvulas.patchEval.onlyAddNewResults=true -Dvulas.patchEval.basefolder=<csv_folder> -Dvulas.patchEval.bugId=<comma_separated_list_of_bugs> -Dvulas.shared.cia.serviceUrl=@@ADDRESS@@/cia -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/-Xmx6G -Xms6G -jar patch-lib-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar
```

Run for CVE-2018-1111,CVE-2018-2222

```sh
java -Dvulas.patchEval.uploadResults=true -Dvulas.patchEval.onlyAddNewResults=true -Dvulas.patchEval.basefolder=<csv_folder> -Dvulas.patchEval.bugId=CVE-2018-1111,CVE-2018-2222 -Dvulas.shared.cia.serviceUrl=@@ADDRESS@@/cia -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/-Xmx6G -Xms6G -jar patch-lib-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar
```

## Manual Library Assessment

The manual assessment can be done from the bugs frontend @@ADDRESS@@/bugs, by setting the assessment to the appropriate value in the "Assessment (Manual)" column and clicking the "Save" button.

<center class='expandable'>
    [![start_page](./img/manual_assessment.jpg)](./img/manual_assessment.jpg)
</center>

Our recommendation is to always rely on code (manually inspecting it in the worst case) in order to take a decision. The versions indicated in the vulnerability's description were proved wrong in multiple cases.

The column "Patch eval" shows information about the results (if any) of the patch lib analyzer. By clicking on the cell the results obtained by code comparison for each elements of the bug change list are shown. If available, it is recommended to use them in order to take a decision about the vulnerability of the corresponding library version.

!!! warning "What do the 'Orange Hourglass' icons mean?"
	Whenever an application library contains the signature of a construct that was changed to fix a vulnerability, but the patch lib analyzer didn't yet (or could not) establish whether it contains the vulnerable or fixed version of the construct, then the tool reports the vulnerability in the web frontend with an ORANGE hourglass in the column "Inclusion of vulnerable code".

	To resolve orange hourglasses the tool needs to know whether the library contains the vulnerable or fixed version of the construct changed in the vulnerability fix, which can be determined [manually](./manuals/library_assessment/#manual-library-assessment) or [automatically](/manuals/library_assessment/#automated-library-assessment).
