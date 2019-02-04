# Workspaces

A workspace acts as a container to group the results of several application analyses.

## Prerequisites

1. URL of the apps Web frontend: @@ADDRESS@@/apps

## Create workspace

Proceed as follows to create a workspace. Note that by creating a workspace, you will also generate a token to be used when performing actual application scans.

1. Connect to the apps Web frontend: @@ADDRESS@@/apps

2. Click on the third button in the lower left corner:

<center>![Setup workspace button](../../tutorials/img/frontend_toolbar_create_workspace.png)</center>

3. Enter the following workspace properties:

- **Name**: Must be provided. Note that the name is not unique, thus, there can be several workspaces having the same name.
- **Description**: Must be provided.
- **Contact**: Please provide the email address of a distribution list (DL).
- **Export results**:
    * AGGREGATED: Findings are aggregated on workspace-level, one item for the entire workspace will be exported
    * DETAILED: Findings are aggregated on application-level, one item for every application of the workspace will be exported
    * OFF: No export of findings

- **Public**: A public workspace appears in the drop-down box of the apps Web frontend, a private one does not. **Important:** It will not be possible to retrieve the token of a private space at later points in time, thus, note it down when it is shown right after workspace creation.

Once the fields have been filled, press the "Save" button. The system then returns with a pop-up containing the random **token** generated upon workspace creation. Do not forget to note down this token, it will be needed later on for each scan of your application(s).

## Use workspace

To use a workspace, you need to pass the token as configuration parameter `vulas.core.space.token`.

See [here](../../../user/manuals/setup/#setup) for more information on how-to configure the scans clients or [continue with the scan tutorials](../../tutorials).
