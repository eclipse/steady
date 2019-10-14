
[![Generic badge](https://img.shields.io/badge/version-1.0.0-green.svg)](https://shields.io/)
# Vulas deployment to k8s
## Folder structure and Overall Conventions

### **Subchart templates**  

The vulas deployment is composed of three charts:
- vulas-core : which encapsulates all its core functions
- vulas-monitoring : which encapsulates its monitoring stack
- vulas-admin : which encapsulates admin functionalities

All values in this helm chart and its subcharts are in camelCase (for example *someVariable*). As every subcharts should be a standalone implementation of a helm charts, each one has an individual set of helper templates mainly:
- _getter.tpl : contains all basic name composition templates
- _labels.tpl : contains all basic label creation templates

Along with these helpers, they also share global ones located in `shared` (with a simple soft symlink) to avoid redundancy.

### .**Subchart composition**  
As declartaions are numerous, the following encapsulating folder scheme has been devised:
- if the subchart contains different distinct application that require each other to function, it will be put in its own repo.
- Each file will be named to represent best its object kind (for example configMap.yaml) writen in camelCase.

For example:
```
.
├── Chart.yaml
├── README.md
├── templates
│   ├── kube-state-metrics
│   │   ├── clusterRoleBinding.yaml
│   │   ├── clusterRole.yaml
│   │   ├── deployment.yaml
│   │   ├── _getters.tpl
│   │   ├── _labels.tpl
│   │   ├── serviceAccount.yaml
│   │   └── service.yaml
│   ├── node-exporter
│   │   ├── daemonSet.yaml
│   │   ├── _getters.tpl
│   │   ├── _labels.tpl
│   │   ├── podSecurityPolicy.yaml
│   │   ├── roleBinding.yaml
│   │   ├── role.yaml
│   │   ├── serviceAccount.yaml
│   │   └── service.yaml
│   ├── NOTES.txt
│   └── prom-server
│       ├── clusterRoleBinding.yaml
│       ├── clusterRole.yaml
│       ├── configMap.yaml
│       ├── _getters.tpl
│       ├── _labels.tpl
│       ├── serviceAccount.yaml
│       ├── serviceHeadless.yaml
│       ├── service.yaml
│       └── statefulSet.yaml
├── _values.yaml
└── values.yaml
```

### .**Helpers**  
Each helper functions header contains the name of said function along with a short description of its purpose, its usage context (Note that if is says `contexts: [ . ]` it can only be used within the subcharts contexts) and an example usage.

Within reasonable limits, all helper functions must be :
- limited to its subcharts scope
- preceded by the chart's name
- camelCase
- refrain from verbs as a whole for naming
- indicate which object it targets

(like *subchart.objectName*).

### .**Templating Object Declarations**

In order to garantee a coherent and consistent declaration convention, each yml should loosely follow this order (line jumps indicated with #), explicited for controllers :

```yaml
apiVersion: {{ .apiVersion }}
kind: {{ .objectKind }}
#
metadata:
  name: {{{ template "subchart.objectName" . }}
  labels:
    app.kubernetes.io/name : {{ template "subchart.objectName" . }}
    app.kubernetes.io/part-of: {{ include "subchart.name" . }}
    app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
    app.kubernetes.io/managed-by: {{ .Release.Service }}
#
spec:
  replicas: {{ .replicas }}
#
  template:
    metadata:
      labels:
        app.kubernetes.io/name : {{ template "subchart.objectName" . }}
        app.kubernetes.io/part-of: {{ include "subchart.name" . }}
        app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
        app.kubernetes.io/managed-by: {{ .Release.Service }}
        {{ include "subchart.objectName.otherLabels" . }}
    #
    spec:
      volumes:
      - name: ---
        configMap:
          name: ---
      #
      initContainers:
        - name: ---
          image: ---
          imagePullPolicy: ---
          command: ---
          #
          envFrom:
            - configMapRef: ---
                name: ---
          #
          securityContext: --- # User
          securityContext: --- # Linux capabilities
      #
      Containers:
        - name: ---
          image: ---
          imagePullPolicy: ---
          #
          ports: ---
          #
          envFrom: ---
          #
          livenessProbe: ---
          #
          readinessProbe: ---
```

With emphasis on when it comes to declarations:
- version of release on each object
- envFrom over env
- livenessProbe and readinessProbe preferably different
- ports always having names
- imagePullPolicy always indicated

When it comes to templating:
- prefer `nindent` over `indent`
- when including templates retain the original tabulations for readability
- prefer `include` over `template` when not inline and vice versa

When it comes to writing conditional and complex values
- prefer using `with` or `$variable:=` to shorten naming scheme
- when checking if a layered variable exist use the staircase method (to garantee passing the `helm lint --strict`), for example:
```
{{- if .Values.a }}
{{- if .Values.a.b }}
{{- end }}
{{- end }}
```
