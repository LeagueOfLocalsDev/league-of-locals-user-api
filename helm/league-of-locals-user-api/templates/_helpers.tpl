{{- define "league-of-locals-user-api.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "league-of-locals-user-api.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- include "league-of-locals-user-api.name" . }}
{{- end }}
{{- end }}

{{- define "league-of-locals-user-api.labels" -}}
app.kubernetes.io/name: {{ include "league-of-locals-user-api.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" }}
{{- end }}

{{- define "league-of-locals-user-api.selectorLabels" -}}
app.kubernetes.io/name: {{ include "league-of-locals-user-api.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
