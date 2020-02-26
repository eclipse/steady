# Populating database inside the Kubernetes cluster

This guide is destined to those who want to populate their vulnerability databases with the open sourced [knowledge base](https://github.com/SAP/vulnerability-assessment-kb).
This can be automated using the utils packaged with this chart (which can be installed by following this [guide](../utils/README.md)). You can run the following command, once the module is built:

```sh
utils load <bugFile.yaml> \
  --concurrent { Concurrent } \
  --releaseName { releaseName } \
  --namespace { namespace } \
  --skip

# for more info and flags
utils load --help

# for cleaning up the pods after a run
kubectl delete pods -l 'app.kubernetes.io/part-of=bugs-loader' -n { namespace }
```

## . Configuration
As off utils release v0.0.1 the bug file has to be a **yaml** file following this structure:
```yaml
bugs:
- reference: bug1   # vulnerability identifier
  repo: repo        # URL of the VCS repository hosting the library project

  # One or multiple revisions (multiple ones must be comma-separated w/o blanks).
  # In the case of Git repositories, the revision can be optionally
  # concatenated with ,)
  commit: "id"        

  # (optional, it must be provided for vulnerabilities not available from the NVD) )
  # Comma-separated list of links to comprehensive vulnerability information
  links: "links"

  # (optional)
  description: "Lorem ipsum dolor sit amet..."
```

## . Behind the scenes

This module uses Golang routines to split the bugs list into **n** (the amount of concurrent jobs) equal chunks. In short, these will be fed into the patch-analyzer which will then analyze the codes and push it into the restbackend accordingly.

In reality, this module generates a shell script which contains call to the patchanalyzer jar and mounts it via configmap to a list of jobs, each having different configmaps corresponding to the chunk that they're in charge of.

Then it generates a list of routines that watch the job for event changes and cleans up if the job fails, succeedes or gets deleted
