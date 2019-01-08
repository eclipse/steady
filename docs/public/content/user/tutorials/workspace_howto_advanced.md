# Workspaces

If an application has been already released to customers and, at the same time, new releases are under development, you may want to setup workspaces as follows:

* One workspace for the development branch, to prevent that vulnerable dependencies are introduced at development time
* One workspace for every release branch, to monitor whether there are new vulnerabilities for dependencies of production releases
