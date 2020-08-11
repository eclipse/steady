{{/*---------------------------- Local Templates ----------------------------*/}}
{{/*
  pgpool.name : Gets name of current component
  contexts: [ . ]
  usage: {{ include "pgpool.name" . }}
*/}}
{{- define "pgpool.name" -}}
{{ .Release.Name }}-pgpool
{{- end -}}

{{/*
  pgpool.configMapName : ConfigMap getter
  contexts: [ . ]
  usage: {{ include "pgpool.configMapName" . }}
*/}}
{{- define "pgpool.configMapName" -}}
{{- $name := include "pgpool.name" . -}}
{{- printf "%s-config" $name -}}
{{- end -}}

{{/*
  pgpool.secretName : Secret getter
  contexts: [ . ]
  usage: {{ include "pgpool.secretName" . }}
*/}}
{{- define "pgpool.secretName" -}}
{{- $name := include "pgpool.name" . -}}
{{- printf "%s-secret" $name -}}
{{- end -}}

{{/*
  pgpool.podName : deployment pod name getter
  contexts: [ . ]
  usage: {{ include "pgpool.podName" . }}
*/}}
{{- define "pgpool.podName" -}}
{{- $name := include "pgpool.name" . -}}
{{- printf "%s-pod" $name -}}
{{- end -}}


{{/*
  pgpool.priorityClassName : priorityClassName pod name getter
  contexts: [ . ]
  usage: {{ include "pgpool.priorityClassName" . }}
*/}}
{{- define "pgpool.priorityClassName" -}}
{{- $name := include "pgpool.name" . -}}
{{- printf "%s-priority-class" $name -}}
{{- end -}}

{{/*
  pgpool.podDisruptionBudgetName : podDisruptionBudgetName pod name getter
  contexts: [ . ]
  usage: {{ include "pgpool.podDisruptionBudgetName" . }}
*/}}
{{- define "pgpool.podDisruptionBudgetName" -}}
{{- $name := include "pgpool.name" . -}}
{{- printf "%s-pod-disruption-budget" $name -}}
{{- end -}}


{{/*
  pgpool.statefulSetName : statefulset name getter
  contexts: [ . ]
  usage: {{ include "postgres.statefulSetName" . }}
*/}}
{{- define "pgpool.statefulSetName" -}}
{{- $name := include "pgpool.name" . -}}
{{- printf "%s-stateful-set" $name -}}
{{- end -}}
