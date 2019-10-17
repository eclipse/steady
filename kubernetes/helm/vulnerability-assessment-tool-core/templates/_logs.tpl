{{/*-------------------------- Global Templates ----------------------------*/}}
{{/*
  logFunctions : generic logging function
  contexts: [ . ]
  usage: {{ include "logFunctions" . }}
*/}}

{{- define "logFunctions" -}}
_vulas_log() {
  echo `date "+%Y:%m:%d-%H:%M:%S"` "[$VULAS_CHART|$VULAS_LOG_ORIGIN]" "$1": "$2"
}

_vulas_error() {
  _vulas_log "ERROR" "$1"
}

_vulas_info() {
  if [ -z $VULAS_DEBUG ]; then
    _vulas_log "INFO" "$1"
  fi
}
{{- end -}}
