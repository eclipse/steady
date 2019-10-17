{{/*
  commonLabels : generic labels
  contexts: [ . ]
  usage: {{ include "commonLabels" . }}
*/}}

{{- define "commonLabels" -}}
{{ if .Chart.AppVersion -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/part-of: {{ .Values.global.projectName }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
{{ include "projectLabels" . }}
{{- end -}}


{{/*
  projectLabels : generic labels
  contexts: [ . ]
  usage: {{ include "projectLabels" . }}
*/}}
{{- define "projectLabels" -}}
{{ .Values.global.projectName }}/environment: {{ .Values.global.env }}
{{ .Values.global.projectName }}/release-name: {{ .Release.Name }}
{{- end -}}
