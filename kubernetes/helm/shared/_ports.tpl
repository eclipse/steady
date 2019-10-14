
{{/*
  listPorts : lists port
  contexts: [ .global ]
  usage: {{ include "listPorts" . }}
*/}}
{{- define "listPorts" -}}
{{- range $key, $value := . }}
- name: {{ $key }}
  {{- toYaml . | nindent 2 }}
{{- end -}}
{{- end -}}
