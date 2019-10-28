# plugin-setuptools

The vulnerability-assessment-tool plugin for Python allows to scan a Python application developed with setuptools.

Notes:

* The plugin is in beta, use with care and provide us feedback

## Install the vulnerability-assessment-tool plugin

Until the plugin is available in PyPI, it has to be installed from the sources. Clone this repo and run the following:

```
cd plugin-setuptools
python setup.py install
```

## Scan your application

Until now, only the `app`` goal is supported, the other vulnerability-assessment-tool goals will be added step-by-step.
Feel free to volunteer :)

### Create a method-level BOM

Create a file `vulas-python.cfg` in the project's root folder. It must contain the following information, further configuration settings can be added if necessary.

```ini
vulas.shared.backend.serviceUrl = http:/localhost:8033/backend
```

Then run the following command:

```sh
python setup.py app
```
