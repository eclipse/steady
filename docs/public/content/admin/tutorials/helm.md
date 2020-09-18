# Deploy on Kubernetes with Helm

In this tutorial you will be guided through the necessary steps to set-up the @@PROJECT_NAME@@ services in a Kubernetes cluster using [Helm](https://helm.sh/) version [3.0.0-beta4](https://github.com/helm/helm/releases/tag/v3.0.0-beta.4) and is compatible with any helm2 version [2.15.0](https://github.com/helm/helm/releases/tag/v2.15.0).

!!! info "Helm2 and Helm3"

    `Helm2` has officially gone into maintenance mode (no further release after 2.15.0) but this chart is meant to be compatible with both major versions of Helm. The main difference being that in `helm 3` tiller is no longer required and all charts are namespaced.


## Pre-requisites

-   git
-   Helm
-   Kubernetes >=v1.15.0 with Beta APIs enabled

## Setup

![helm-chart](../../images/helm_architecture.png)

This repository contains three charts:  

-   **vulnerability-assessment-tool-core**: which encapsulates all the core components of the tool  
-   **vulnerability-assessment-tool-admin**: which encapsulates all the cluster admin tools (namely the ingress controller to expose the service)  
-   **vulnerability-assessment-tool-monitoring**: which is used to deploy the dedicated monitoring stack  

Clone locally `vulnerability-assessment-tool` repository and change the directory to the Helm chart's folder

```sh
git clone https://github.com/eclipse/steady.git
cd steady/kubernetes/helm
```

Modify the `values.yaml` files and edit them to match your needs.
In order to verify the validity of your values and get a preview of your generated chart:

```sh
helm template vulnerability-assessment-tool-core
helm template vulnerability-assessment-tool-admin
helm template vulnerability-assessment-tool-monitoring
```

## Run

You are now ready to deploy @@PROJECT_NAME@@ inside your Kubernetes cluster:

### Vulnerability-assessment-tool-core chart

```sh
# For helm 2
helm install vulnerability-assessment-tool-core/ --name ReleaseName

# For helm 3
helm install vulnerability-assessment-tool-core ReleaseName
```

The above command will create a `Namespace` called `vulnerability-assessment-tool-core` (which can be specified in the [vulnerability-assessment-tool-core/values.yaml](https://github.com/eclipse/steady/blob/master/kubernetes/helm/vulnerability-assessment-tool-core/values.yaml)) and install all the component on it. To check if everything is starting successfully you can watch the deployments by running the command `kubectl get pods -n vulnerability-assessment-tool-core`. The deployment will request a couple `PersistentVolumeClaims` which could need some time to be created depending on the cloud provider you are running on.


### Vulnerability-assessment-tool-monitoring chart

```sh
# For helm 2
helm install vulnerability-assessment-tool-monitoring/ --name ReleaseName

# For helm 3
helm install vulnerability-assessment-tool-monitoring ReleaseName
```

The above command will create a `Namespace` called `vulnerability-assessment-tool-monitoring` (which can be specified in the [vulnerability-assessment-tool-monitoring/values.yaml](https://github.com/eclipse/steady/blob/master/kubernetes/helm/vulnerability-assessment-tool-monitoring/values.yaml)) and install all the component on it. To check if everything is starting successfully you can watch the deployments by running the command `kubectl get pods -n vulnerability-assessment-tool-monitoring`. The deployment will request a couple `PersistentVolumeClaims` which could need some time to be created depending on the cloud provider you are running on.

!!! info "Monitoring scope"

    This chart's monitoring is not scoped to any namespace so you can add other pods to be monitored simply by adding `prometheus.io/scrape: "true"` in the correct pod annotation and in which ever namespace you desire.

### Vulnerability-assessment-tool-admin chart

```sh
# For helm 2
helm install vulnerability-assessment-tool-admin/ --name ReleaseName

# For helm 3
helm install vulnerability-assessment-tool-admin ReleaseName
```

The above command will create a `Namespace` called `vulnerability-assessment-tool-admin` (which can be specified in the [vulnerability-assessment-tool-admin/values.yaml](https://github.com/eclipse/steady/blob/master/kubernetes/helm/vulnerability-assessment-tool-admin/values.yaml)) and install all the component on it. To check if everything is starting successfully you can watch the deployments by running the command `kubectl get pods -n vulnerability-assessment-tool-admin`. The deployment will request a `LoadBalancer` which could need some time to be created depending on the cloud provider you are running on.

!!! warning "Reaching @@PROJECT_NAME@@ from the Internet"

    This chart creates a `LoadBalancer` to allow Internet traffic to reach the cluster provisioned by your provider and will connect to it. This `LoadBalancer` will be exposed on the Internet so be careful to change the authentication ingress values from the default ones. The `Service type:LoadBalancer` should work with most providers such as GKE, Azure. If not, you can follow this [ingress-nginx guide](https://github.com/kubernetes/ingress-nginx/blob/master/docs/deploy/index.md#provider-specific-steps)
