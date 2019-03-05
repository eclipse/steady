# Getting started with the Vulnerability Database

Vulnerabilities in @@PROJECT_NAME@@ are represented at code level. This tutorial will guide you through the creation of the following vulnerabilities in the vulnerability database:

- [CVE-2017-7525](https://nvd.nist.gov/vuln/detail/CVE-2017-7525) (Jackson Databind)
- [CVE-2018-5382](https://nvd.nist.gov/vuln/detail/CVE-2018-5382) (BouncyCastle)
- [CVE-2018-11039](https://nvd.nist.gov/vuln/detail/CVE-2018-11039) (Spring)
- [CVE-2014-0050](https://nvd.nist.gov/vuln/detail/CVE-2014-0050) (Apache Commons FileUpload)
- [COLLECTIONS-580](https://issues.jboss.org/browse/JBDS-3560?attachmentViewMode=list&_sscc=t) (Apache Commons Collections)

The tutorial also explains how to consume the import script available in the [knowledge base](https://github.com/SAP/vulnerability-assessment-kb). Such script triggers the analysis of the available vulnerabilities.

If you have a working installation of the @@PROJECT_NAME@@ backend services you will need
to import vulnerability data before you can actually perform scans.

!!! tip "Installing the @@PROJECT_NAME@@ backend services"

    Instructions on how to setup backend services are available [here](../../../admin/).

It is possible to add new vulnerabilities to the database using the `patch-analyzer` module of the @@PROJECT_NAME@@ project.

## Patch Analyzer

### Creation of above-listed vulnerabilities

Run the following commands to create the change list for the vulnerability and upload it to the backend

```sh
java -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/-jar ./patch-analyzer/target/patch-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar com.sap.psr.vulas.PatchAnalyzer -r https://github.com/FasterXML/jackson-databind -b CVE-2017-7525 -e e8f043d1aac9b82eee907e0f0c3abbdea723a935,ddfddfba6414adbecaff99684ef66eebd3a92e92,60d459cedcf079c6106ae7da2ac562bc32dcabe1 -links https://github.com/FasterXML/jackson-databind/issues/1599,https://github.com/FasterXML/jackson-databind/issues/1680,https://github.com/FasterXML/jackson-databind/issues/1737  -descr "When configured to enable default typing, Jackson contained a deserialization vulnerability that could lead to arbitrary code execution. Jackson fixed this vulnerability by blacklisting known 'deserialization gadgets'. This vulnerability solves an incomplete fix for CVE-2017-4995-JK (main description at: https://github.com/FasterXML/jackson-databind/issues/1599 Issues not addressed by the incomplete fix of CVE-2017-4995-JK: https://github.com/FasterXML/jackson-databind/issues/1680 and https://github.com/FasterXML/jackson-databind/issues/1737) " -u

java -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/-jar ./patch-analyzer/target/patch-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar com.sap.psr.vulas.PatchAnalyzer -r https://github.com/bcgit/bc-java/ -b CVE-2018-5382 -e 81b00861cd5711e85fe8dce2a0e119f684120255 -links https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-31659,https://www.kb.cert.org/vuls/id/306792 -u

java -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/-jar ./patch-analyzer/target/patch-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar com.sap.psr.vulas.patcha.PatchAnalyzer -r https://github.com/spring-projects/spring-framework/ -b CVE-2018-11039 -e dac97f1b7dac3e70ff603fb6fc9f205b95dd6b01,f2694a8ed93f1f63f87ce45d0bb638478b426acd -links https://pivotal.io/security/cve-2018-11039,https://jira.spring.io/browse/SPR-16836 -descr "Cross Site Tracing (XST) with Spring Framework. Description: Spring Framework (versions 5.0.x prior to 5.0.7, versions 4.3.x prior to 4.3.18, and older unsupported versions) allow web applications to change the HTTP request method to any HTTP method (including TRACE) using the HiddenHttpMethodFilter in Spring MVC. If an application has a pre-existing XSS vulnerability, a malicious user (or attacker) can use this filter to escalate to an XST (Cross Site Tracing) attack. Affected Pivotal Products and Versions: Spring Framework 5.0 to 5.0.6, Spring Framework 4.3 to 4.3.17, Older unsupported versions are also affected. Mitigation: Users of affected versions should apply the following mitigation: \n 5.0.x users should upgrade to 5.0.7. \n- 4.3.x users should upgrade to 4.3.18. - Older versions should upgrade to a supported branch. \nThere are no other mitigation steps necessary. This attack applies to applications that: \n1/ Use the HiddenHttpMethodFilter (it is enabled by default in Spring Boot). \n2/ Allow HTTP TRACE requests to be handled by the application server. \nThis attack is not exploitable directly because an attacker would have to make a cross-domain request via HTTP POST, which is forbidden by the Same Origin Policy. This is why a pre-existing XSS (Cross Site Scripting) vulnerability in the web application itself is necessary to enable an escalation to XST." -u

java -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/-jar ./patch-analyzer/target/patch-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar com.sap.psr.vulas.PatchAnalyzer -r https://github.com/apache/commons-fileupload -b CVE-2014-0050 -e c61ff05b3241cb14d989b67209e57aa71540417a -u

java -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/-jar ./patch-analyzer/target/patch-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar com.sap.psr.vulas.PatchAnalyzer -r https://github.com/apache/commons-collections -b COLLECTIONS-580 -e 3eee44cf63b1ebb0da6925e98b3dcc6ef1e4d610,78d47d4d098ab814a7a00a0b1c81646b27f050cf,e585cd0433ae4cfbc56e58572b9869bd0c86b611,b2b8f4adc557e4ef1ee2fe5e0ab46866c06ec55b,da1a5fe00d79e1840b7e52317933e9eb56e88246,1642b00d67b96de87cad44223efb9ab5b4fb7be5:3_2_X,5ec476b0b756852db865b2e442180f091f8209ee:3_2_X,bce4d022f27a723fa0e0b7484dcbf0afa2dd210a:3_2_X,d9a00134f16d685bea11b2b12de824845e6473e3:3_2_X -descr "Arbitrary remote code execution with InvokerTransformer. With InvokerTransformer serializable collections can be build that execute arbitrary Java code. sun.reflect.annotation.AnnotationInvocationHandler#readObject invokes #entrySet and #get on a deserialized collection. If you have an endpoint that accepts serialized Java objects (JMX, RMI, remote EJB, ...) you can combine the two to create arbitrary remote code execution vulnerability. Fixed in versions 3.2.2, 4.1" -links https://issues.apache.org/jira/browse/COLLECTIONS-580,https://commons.apache.org/proper/commons-collections/security-reports.html -u
```

To run such commands in your own environment, you may need to adapt:

* the path to the patch analyzer artifact and the artifact version, `-jar ./patch-analyzer/target/patch-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar`
* the URL of the @@PROJECT_NAME@@ backend, `-Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/`
* the proxy settings; the proxy is not configured in the example but you can do so using the following configurations:

```sh
-Dhttp.proxyHost=
-Dhttp.proxyPort=
-Dhttps.proxyHost=
-Dhttps.proxyPort=
-Dhttp.nonProxyHosts=
```

### Batch Import from Knowledge Base

To run the batch import you need to :
 
- Download the script "import_vulas_kb.sh" from the [knowledge base](https://github.com/SAP/vulnerability-assessment-kb) 
- Move it to the same folder where the patch-analyzer built artifact is available (Note that the artifact must be named `patch-analyzer-jar-with-dependencies.jar`)
- Run  the following command providing as argument the url of the backend

```sh
bash import_vulas_kb.sh @@ADDRESS@@/backend/
```


!!! Info
	The patch-analyzer artifact can be found in the `/target` folder of the `patch-analyzer` module after building the project with maven, or in the `docker/client-tools` folder as a result of the build described [here](../../../admin/). In both cases the file needs to be renamed by removing the version information into `patch-analyzer-jar-with-dependencies.jar`.

## Patch Lib Analyzer

Once vulnerabilities are created in the vulnerability database, the patch lib analyzer must run periodically to assess whether all versions of the library known by the backend contain the vulnerable or fixed version of the construct.

!!! Info
	To get the best of the tutorial, we recommend to proceed to the next step only after having analyzed at least one application (see [tutorial page](../../../user/tutorials/)).

Run for all bugs

```sh
java -Dvulas.patchEval.uploadResults=true -Dvulas.patchEval.onlyAddNewResults=true -Dvulas.patchEval.basefolder=<csv_folder> -Dvulas.patchEval.bugId=<comma_separated_list_of_bugs> -Dvulas.shared.cia.serviceUrl=@@ADDRESS@@/cia -Dvulas.shared.backend.serviceUrl=@@ADDRESS@@/backend/-Xmx6G -Xms6G -jar patch-lib-analyzer-@@PROJECT_VERSION@@-jar-with-dependencies.jar
```

To check how to run it as a job, check [here](../../manuals/patch_lib_analyzer)

!!! Info
	In case the application results still shows orange hourglasses after running the patch lib analyser, the manual assessment is required, see [here](../../manuals/manual_assessment/)
