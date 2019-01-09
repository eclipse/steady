# Documentation

## Overview

This folder contains the sources of the documentation of vulnerability-assessment-tool.

The documentation is generated with a script [docs.py](./docs.py) which is a wrapper on the build system that we use ([MkDocs](./mkdocs.yml)). With this script one can also generate custom documentations, we use this script to generate our internal documentation for SAP's employees.

## Setup

### Preliminary steps (one-time only)

You will need a working python 3 installation (tested with 3.7) and a [few packages](./requirements.txt).

```sh
python -m pip install -r requirements.txt
```

### Editing public docs

Just use your favorite text editor and modify the files in the `public/content` folder.

### Reviewing changes

In a console, type the following:

```sh
python docs.py public --mkserve
```

Then visit http://localhost:8000 to see the result of your edits.

_TODO: When you save changes the browser will reload_

### Publishing

```sh
python docs.py public --mkghdeploy
```

### Enterprise docs

With the `docs.py` one can create a custom version of the public docs with new private pages or modified pages for their enterprise employees. In order to do so, a new `git` repository should be created in your company. This new `git` repository will have to contain only enterprise-specific files which will overwrite public files.

The enterprise repository should have in his root a:

- `enterprise.properties` to assign values to variables as in [/docs/public/public.properties](./public.properties)
- custom `mkdocs.yml`
- `content` folder with the same skeleton as in [/docs/public/content](./public/content)

_The script will merge the enterprise docs on top of the public ones. All the files with the same name/path will be overwritten and the files which end by `_enterprise` will be added to the docs._

#### Build enterprise docs

By default the `docs.py` script will clone the enterprise repository and merge it with the public one in order to create enterprise docs. If you want you can instead pass as a parameter a local path to the enterprise repository for easier debug of the docs.

```sh
python docs.py enterprise --url https://github.xxx/yyy/zzz.git --mkserve
# Use the command below if you dont want the script to clone the enterprise repo. Be sure to have the enterprise repo up to date
python docs.py enterprise --url https://github.xxx/yyy/zzz.git --local_repo ../zzz --mkserve
```

#### Publish enterprise docs

The `docs.py` script is also able to publish enterprise docs to enterprise Github pages, from this repository.

```sh
python docs.py enterprise --url https://github.xxx/yyy/zzz.git --mkghdeploy
# We recomment to always let the script clone the enterprise private repository instead of linking to a local directory
python docs.py enterprise --url https://github.xxx/yyy/zzz.git --local_repo ../zzz --mkghdeploy
```

## Test

A [script](./checklinks.sh) is provided in order to check if all the links in the generated documentation are pointing to existent resources.

The script uses the [Muffet](https://github.com/raviqqe/muffet) library, installation instruction are provided in the library's README.

The `checklinks.sh` script scrapes the generated docs and reports broken links.

```sh
# Serve the docs
python docs.py public --mkserve
```

```sh
# Find broken links
sh checklinks.sh
```
