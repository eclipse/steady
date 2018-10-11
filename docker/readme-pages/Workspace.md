A workspace acts as a container to group the results of several application analyses. In that context, please note that each module of a multi-module Maven project appears as a separate application in the Vulas Web frontend.

Before using workspaces, you need to create one using the Vulas Web frontend (see below); you will obtain a token that you will need to pass as a configuration parameter when performing scans. 

## Create workspace(s) (once)

You need to create a workspace once using the Vulas Web frontend. Use the plus icon in the lower-left corner of the apps Web frontend to do so. Do not forget to note down the random **token** generated upon workspace creation.

A workspace has the following properties (the mandatory ones are flagged with *):
- ***Name**
- ***Description**
- **Contact**: Please provide the email address of a distribution list (DL) matching the regex configured in the rest-backend properties
- **Export results**:
    * AGGREGATED: Findings are aggregated on workspace-level, one item for the entire workspace will be exported 
    * DETAILED: Findings are aggregated on application-level, one item for every application of the workspace will be exported
    * OFF: No export of findings
- **Public**: A public workspace appears in the drop-down box of the apps Web frontend, a private one does not

If an application has been already released to customers and, at the same time, a new release is under development, you may want to setup workspaces as follows:
* One workspace for the development branch, to prevent that vulnerable dependencies are introduced at development time
* One workspace for every release branch, to monitor whether there are new vulnerabilities for dependencies of production releases

## Use the generated token (during application analysis)

Use the workspace token as value for the configuration setting `vulas.core.space.token`. See [here](Configuration.md) for more information regarding the configuration of Vulas clients.

## Choose a workspace (in the apps Web frontend)

Use the configuration icon in the lower-left corner in order to select a workspace. Once selected, the list of applications of that workspace will be loaded automatically.

You can search for _public_ workspaces by typing their name. You can copy&paste the token of a _private_ workspace into the input field in order to select it. 

## REST API

The REST API can be used to export findings in a machine-readable fashion, its data model has been driven by the initial requester. Calling HTTP GET on the following URL returns all vulnerable dependencies of an aggregated workspace:

`https://<host>/backend/hubIntegration/apps/<workspace-name>%20(<workspace-token>)/vulndeps`

The API returns an array of JSON elements having the following data model:

| Property | Description | Possible Values |
|---|---|---|
| projectId | Identifies the affected application and dependency (`<app-GAV> > <dep-filename>`) ||
| type | Identifier of vulnerability in Vulas knowledge base ||
| scope | Scope of the dependency | see [official Maven documentation](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope) | 
| priority | Priority of the finding | 3 in case of dependencies with scope TEST and PROVIDED, 1 otherwise |
| exemptionReason | Assessment description (if any), see [report goal](Java.md) for more information on how-to assess and exempt findings ||
| state | Assessment result (if any) | 1 (secure-by-design) in case of dependencies with scope TEST and PROVIDED, 4 (mitigated) in case the bug has been exempted, 2 (true-positive) otherwise |
| status | Indicates whether the finding has been assessed  | 1 (audited) in case state is 1 or 4, -1 (non-audited) otherwise |
| count | Number of findings of type `type` in project | Always 1 |
| snapshotDate | Date of most recent goal execution of the application (any goal) ||


**Important:** Unless you do some tests, please create a dedicated workspace for your application.
