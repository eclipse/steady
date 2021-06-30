Python applications can be analyzed either using the Steady plugin for [Setuptools](https://setuptools.readthedocs.io/en/latest/) or using the Steady CLI, both options are described below.

# Steady plugin for Setuptools

## Prerequisites

- The Python packages `pip` and `virtualenv` are installed
- The token of a Steady [workspace](Workspace.md) is known
- `setup.py` is present
- Java is installed

## Setup

1. Create a file `vulas-python.cfg` in the root directory of the Python project. It should contain at least the following configuration settings (you may want to replace `localhost` with the hostname of the vulas backend):

        vulas.core.space.token = <YOUR WORKSPACE TOKEN>
        vulas.shared.backend.serviceUrl = http://localhost:8033/backend
        vulas.core.backendConnection = READ_WRITE

2. Install the plugin: `pip install --upgrade vulas-plugin-setuptools`. 

## Goal execution

As of today, the plugin supports the _app_, _clean_ and _report_ goal. The static and dynamic analyses (_a2c_, _test_, _t2c_) will be added at a later point. The deletion of workspaces (_cleanSpace_) can be done using the CLI. See [here](goals) for a description of all analysis goals.

### app

1. Run `python setup.py app`

2. Connect to the apps Web frontend, then select your workspace and Python application

### clean

1. Run `python setup.py clean`

### report

1. Run `python setup.py report`

# CLI

## Prerequisites

- The Python package `pip` is installed and "knows" all application dependencies (check with `pip list`)
- The token of a Steady [workspace](Workspace.md) is known
- Java is installed

## Setup

1. Get the Steady CLI from `<vulas-root>/cli-scanner/target/cli-scanner-<version>-jar-with-dependencies.jar`.

2. Create a file `vulas-custom.properties` in the same directory. It should contain at least the following configuration settings:

        # Used to identify the app under analysis
        vulas.core.appContext.group = <GROUP>
        vulas.core.appContext.artifact = <ARTIFACT>
        vulas.core.appContext.version = <VERSION>
        
        vulas.core.space.token = <YOUR WORKSPACE TOKEN>
        vulas.shared.backend.serviceUrl = http://localhost:8033/backend
        vulas.core.backendConnection = READ_WRITE

        # Specify exactly one of the two following settings

        # Directory to search for setup.py files (multiple entries to be separated by comma)
        #vulas.core.app.sourceDir = <PATH-TO-PYTHON-SOURCES>

        # Full path to PIP binary (e.g., global installation, virtual environment or Anaconda)
        #vulas.core.bom.python.pip = <PATH-TO-PIP-BINARY>/pip
        
        # Not relevant for Python (specified to prevent an err message in the Java analyzer)
        vulas.core.app.appPrefixes = com.sap

Additional notes:
* `vulas.core.appContext.group`, `artifact` and `version` uniquely identify your app in the given workspace.
* `vulas.core.app.sourceDir` must point to one or more directories with the Python source files of your app (multiple directories must be separated by comma). Note that the Steady CLI must not be contained in any of them, as it would be added as a dependency to your app.
* `vulas.core.bom.python.pip` must point to a `pip` binary (not the path in which the binary is located), either the global `pip` or one installed in a virtual environment. If set, `pip` will be used right away to determine the dependencies. If empty, any `setup.py` in the source dir(s) will be installed in a virtual environment in order to determine the app dependencies.

## Goal execution

### app

1. Run `java -jar cli-scanner-<version>-jar-with-dependencies.jar -goal app`

2. Connect to the apps Web frontend, then select your workspace and Python application

### clean

1. Run `java -jar cli-scanner-<version>-jar-with-dependencies.jar -goal clean`

### report

1. Run `java -jar cli-scanner-<version>-jar-with-dependencies.jar -goal report`

2. Check the console to see where the Html, Json and Xml reports have been written to

# Troubleshooting

## Symptom: _app_ goal takes time

Dependencies are analyzed by setting up a virtual environment for the respective application. The setup of such a virtualenv can take some time (no matter whether it is done in the context of Steady or not). Moreover, whenever a Python library is found that is not yet known to the backend, its bill of material needs to be uploaded. The delay caused by this initial upload will not occur for subsequent scans.

## Symptom: Console log indicates problem related to virtualenv

Run `virtualenv` from the command line to see whether it is properly installed and working.
