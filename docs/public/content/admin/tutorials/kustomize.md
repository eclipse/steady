# Deploy on Kubernetes with Kustomize

In this tutorial you will be guided through the necessary steps to set-up the @@PROJECT_NAME@@ backend services in a Kubernetes cluster using [Kustomize](https://github.com/kubernetes-sigs/kustomize) version [2.0.3](https://github.com/kubernetes-sigs/kustomize/releases/tag/v2.0.3)

!!! info "Kustomize and Kubectl"

    `kubectl` starting version 1.14 already comes with Kustomize 2.0.3 bundled. As of now Kustomize 3.1.0 is not supported by `kubectl` so it will also not be supported by @@PROJECT_NAME@@

## Pre-requisites

- git
- kubectl
- a Kubernetes cluster

## Setup

Clone locally `Eclipse Steady` repository and change the directory to Kustomize's folder

```sh
git clone https://github.com/eclipse/steady.git
cd steady/kubernetes/kustomize
```

Make a copy of the files in `kubernetes/kustomize/secrets` and edit them to match your needs.

```sh
cp secrets/.env.sample secrets/.env
cp secrets/bugs-frontend-credentials.sample secrets/bugs-frontend-credentials
# edit the above files
```

## Run

You are now ready to deploy @@PROJECT_NAME@@ inside your Kubernetes cluster:

```sh
kubectl apply -k .
```

The above command will create a `Namespace` called `vulnerability-assessment-tool` and install all the component on it. To check if everything is starting successfully you can watch the deployments by running the command `kubectl -n vulnerability-assessment-tool get pods -w`. The deployment will request two `PersistentVolumeClaims` and a `Service type:LoadBalancer` which could need some time to be created depending on the provider you are running on.

!!! warning "Reaching @@PROJECT_NAME@@ from the Internet"

    @@PROJECT_NAME@@ uses a `Service` of type `LoadBalancer` to allow Internet traffic to reach the cluster. This `Service` will request an external `LoadBalancer` to your provider and will connect to it. This `LoadBalancer` will be exposed on the Internet so be careful. The `Service type:LoadBalancer` should work with most providers such as GKE, Azure. If not, you can follow this [ingress-nginx guide](https://github.com/kubernetes/ingress-nginx/blob/master/docs/deploy/index.md#provider-specific-steps)

!!! info "Debugging @@PROJECT_NAME@@ from the local network"

    You can avoid listening to the Internet by disabling the `Service` present in the `services/cloud.yml` file. To disable the file you can just comment the relative line in the root-level `kustomization.yaml` file. You can then connect to the main `Service` by `port-forward`ing with the command `kubectl -n vulnerability-assessment-tool port-forward svc/haproxy-ingress 7000:8080` and then opening your browser at `localhost:8080/apps`

### Populate/maintain the vulnerability database

In order for the tool to detect vulnerabilities, you need to import and analyze them first so that they are available in the tool's vulnerability database. Large part of CVE's and bugs are open sourced in [vulnerability-assessment-kb](https://github.com/SAP/vulnerability-assessment-kb).

Follow the instructions mentioned [here](../../../vuln_db/tutorials/vuln_db_tutorial/#batch-import-from-knowledge-base), to import and build all the vulnerabilities' knowledge.

---

Get going:

1. [Import](../../../vuln_db/tutorials/vuln_db_tutorial/) all the CVEs and bugs in your local database
2. Setup your [workspace](../../../user/manuals/setup/#workspace) (if you don't have one)
3. Become familiar with the various analysis [goals](../../../user/manuals/analysis/) (first time users)
4. Analyze your [Java](../../../user/tutorials/java_maven/) or [Python](../../../user/tutorials/python_cli/) application (on a regular basis)
5. [Assess](../../../user/manuals/assess_and_mitigate/) findings using the apps Web frontend (following every analysis)

Further links:

- [Configure](../../../user/tutorials/) the client-side analysis
- [Automate](../../../user/tutorials/jenkins_howto/) with Jenkins
- [Get help](../../../user/support/) if you run into troubles
