{{/*---------------------------- Local Templates ----------------------------*/}}
{{/*
  postgres.master.enrichedLabels : generic labels
  contexts: [ . ]
  usage: {{ include "postgres.master.enrichedLabels" . }}
*/}}
{{- define "postgres.master.enrichedLabels" -}}
app.kubernetes.io/instance: {{ template "postgres.master.name" .}}
{{ include "commonLabels" . }}
{{ include "postgres.master.serviceLabel" . }}
{{- end -}}

{{/*
  postgres.master.serviceLabels : generic service labels
  contexts: [ . ]
  usage: {{ include "postgres.master.serviceLabels" . }}
*/}}
{{- define "postgres.master.serviceLabels" -}}
{{ include "projectLabels" . }}
{{ include "postgres.master.serviceLabel" . }}
{{- end -}}

{{/*
  postgres.slave.enrichedLabels : generic labels
  contexts: [ . ]
  usage: {{ include "postgres.slave.enrichedLabels" . }}
*/}}
{{- define "postgres.slave.enrichedLabels" -}}
app.kubernetes.io/instance: {{ template "postgres.slave.name" . }}
{{ include "commonLabels" . }}
{{ include "postgres.slave.serviceLabel" . }}
{{- end -}}

{{/*
  postgres.slave.serviceLabels : generic service labels
  contexts: [ . ]
  usage: {{ include "postgres.slave.serviceLabels" . }}
*/}}
{{- define "postgres.slave.serviceLabels" -}}
{{ include "projectLabels" . }}
{{ include "postgres.slave.serviceLabel" . }}
{{- end -}}

{{/*---------------------------- Global Templates ---------------------------*/}}
{{/*
  postgres.master.serviceLabel : generic service label
  contexts: [ .global ]
  usage: {{ include "postgres.master.serviceLabel" . }}
*/}}
{{- define "postgres.master.serviceLabel" -}}
{{ .Values.global.projectName }}.core/service: {{ .Release.Name }}-db-master
{{- end -}}

{{/*
  postgres.slave.serviceLabel : generic service label
  contexts: [ .global ]
  usage: {{ include "postgres.slave.serviceLabel" . }}
*/}}
{{- define "postgres.slave.serviceLabel" -}}
{{ .Values.global.projectName }}.core/service: {{ .Release.Name }}-db-slave
{{- end -}}
