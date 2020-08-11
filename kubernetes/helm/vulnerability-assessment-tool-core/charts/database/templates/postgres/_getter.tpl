{{/*---------------------------- Local Templates ----------------------------*/}}
{{/*
  postgres.master.name : Gets name of current component
  contexts: [ . ]
  usage: {{ include "postgres.master.name" . }}
*/}}
{{- define "postgres.master.name" -}}
{{ template "name" . }}-master
{{- end -}}

{{/*
  postgres.slave.name : Gets name of current component
  contexts: [ . ]
  usage: {{ include "postgres.slave.name" . }}
*/}}
{{- define "postgres.slave.name" -}}
{{ template "name" . }}-slave
{{- end -}}

{{/*
  postgres.master.statefulsetName : Gets name of current component
  contexts: [ . ]
  usage: {{ include "postgres.master.statefulsetName" . }}
*/}}
{{ define "postgres.master.statefulsetName" -}}
{{- $name := include "postgres.master.name" . }}
{{- printf "%s-stateful-set" $name -}}
{{- end -}}

{{/*
  postgres.slave.statefulsetName : Gets name of current component
  contexts: [ . ]
  usage: {{ include "postgres.slave.statefulsetName" . }}
*/}}
{{ define "postgres.slave.statefulsetName" -}}
{{- $name := include "postgres.slave.name" . }}
{{- printf "%s-stateful-set" $name -}}
{{- end -}}


{{/*
  postgres.chart : Create chart name and version as used by the chart label.
  contexts: [ . ]
  usage: {{ include "postgres.chart" . }}
*/}}
{{- define "postgres.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
  postgres.configMapName : ConfigMap getter
  contexts: [ . ]
  usage: {{ include "postgres.configMapName" . }}
*/}}
{{- define "postgres.configMapName" -}}
{{- $name := include "name" . -}}
{{- printf "%s-config" $name -}}
{{- end -}}


{{/*
  postgres.script.configMapName : ConfigMap getter
  contexts: [ . ]
  usage: {{ include "postgres.script.configMapName" . }}
*/}}
{{- define "postgres.script.configMapName" -}}
{{- $name := include "name" . -}}
{{- printf "%s-scripts" $name -}}
{{- end -}}

{{/*
  postgres.secretName : Secret getter
  contexts: [ . ]
  usage: {{ include "postgres.secretName" . }}
*/}}
{{- define "postgres.secretName" -}}
{{- $name := include "name" . -}}
{{- printf "%s-secret" $name -}}
{{- end -}}

{{/*
  postgres.pvcName : pvc name getter
  contexts: [ . ]
  usage: {{ include "postgres._pvcName" .context }}
*/}}
{{- define "postgres._pvcName" -}}
{{- printf "-volume-claim" -}}
{{- end -}}

{{/*
  postgres.master.pvcName : master pvc name getter
  contexts: [ . ]
  usage: {{ include "postgres.master.pvcName" .context }}
*/}}
{{- define "postgres.master.pvcName" -}}
{{- include "postgres.master.name" . -}}
{{- include "postgres._pvcName" . -}}
{{- end -}}

{{/*
  postgres.slave.pvcName : slave pvc name getter
  contexts: [ . ]
  usage: {{ include "postgres.slave.pvcName" .context }}
*/}}
{{- define "postgres.slave.pvcName" -}}
{{- include "postgres.slave.name" . -}}
{{- include "postgres._pvcName" . -}}
{{- end -}}


{{/*
  postgres.master.podDisruptionBudgetName : master podDisruptionBudgetName getter
  contexts: [ . ]
  usage: {{ include "postgres.master.podDisruptionBudgetName" . }}
*/}}
{{- define "postgres.master.podDisruptionBudgetName" -}}
{{- $name := include "postgres.master.name" . -}}
{{- printf "%s-pod-disruption-budget" $name -}}
{{- end -}}
{{/*
  postgres.slave.podDisruptionBudgetName : slave podDisruptionBudgetName getter
  contexts: [ . ]
  usage: {{ include "postgres.slave.podDisruptionBudgetName" . }}
*/}}
{{- define "postgres.slave.podDisruptionBudgetName" -}}
{{- $name := include "postgres.slave.name" . -}}
{{- printf "%s-pod-disruption-budget" $name -}}
{{- end -}}


{{/*
  postgres.master.podName : deployment pod name getter
  contexts: [ . ]
  usage: {{ include "postgres.master.podName" . }}
*/}}
{{- define "postgres.master.podName" -}}
{{- $name := include "postgres.master.name" . -}}
{{- printf "%s-pod" $name -}}
{{- end -}}

{{/*
  postgres.slave.podName : deployment pod name getter
  contexts: [ . ]
  usage: {{ include "postgres.slave.podName" . }}
*/}}
{{- define "postgres.slave.podName" -}}
{{- $name := include "postgres.slave.name" . -}}
{{- printf "%s-pod" $name -}}
{{- end -}}


{{/*
  postgres.master.priorityClassName : priorityClassName getter
  contexts: [ . ]
  usage: {{ include "postgres.master.priorityClassName" . }}
*/}}
{{- define "postgres.master.priorityClassName" -}}
{{- $name := include "postgres.master.name" . -}}
{{- printf "%s-priority-class" $name -}}
{{- end -}}

{{/*
  postgres.slave.priorityClassName : priorityClassName getter
  contexts: [ . ]
  usage: {{ include "postgres.slave.priorityClassName" . }}
*/}}
{{- define "postgres.slave.priorityClassName" -}}
{{- $name := include "postgres.slave.name" . -}}
{{- printf "%s-priority-class" $name -}}
{{- end -}}
