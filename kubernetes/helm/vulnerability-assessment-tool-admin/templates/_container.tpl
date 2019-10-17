{{/*
  containerName : Generates containerName
  contexts: [ .global ]
  usage: {{ include "containerName" . }}
*/}}
{{- define "containerName" -}}
{{- if .registry -}}
{{- printf "%s" .registry -}}
{{- if .registryPort -}}
{{- printf ":%d/" (int .registryPort) -}}
{{- else -}}
{{- printf "/" -}}
{{- end -}}
{{- end -}}
{{- printf "%s:%s" .name .tag }}
{{- end -}}
