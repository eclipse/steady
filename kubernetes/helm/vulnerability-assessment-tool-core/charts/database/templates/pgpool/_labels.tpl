{{/*----------------------------- Local Templates ----------------------------------------*/}}
{{/*
  pgpool.commonLabels : generic labels
  contexts: [ . ]
  usage: {{ include "pgpool.commonLabels" . }}
*/}}
{{- define "pgpool.enrichedLabels" -}}
app.kubernetes.io/instance: {{ template "pgpool.podName" . }}
{{ include "commonLabels" . }}
{{ include "pgpool.serviceLabel" . }}
{{- end -}}

{{/*
  pgpool.serviceLabels : generic service label
  contexts: [ .global ]
  usage: {{ include "pgpool.serviceLabels" . }}
*/}}
{{- define "pgpool.serviceLabels" -}}
{{ include "commonLabels" . }}
{{ include "pgpool.serviceLabel" . }}
{{- end -}}

{{/*---------------------------- Global Templates ---------------------------*/}}
{{/*
  pgpool.serviceLabel : generic service label
  contexts: [ .global ]
  usage: {{ include "pgpool.serviceLabel" . }}
*/}}
{{- define "pgpool.serviceLabel" -}}
{{ .Values.global.projectName }}.core/service: {{ .Release.Name }}-pgpool-service
{{- end -}}
