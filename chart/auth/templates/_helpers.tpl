{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "hs256SecretName" -}}
  {{- if .Values.global.hs256key.secretName -}}
    {{- .Release.Name }}-{{ .Values.global.hs256key.secretName -}}
  {{- else -}}
    {{- .Release.Name }}-{{ .Chart.Name }}-{{ .Values.hs256key.secretName -}}
  {{- end }}
{{- end -}}

{{- define "customerUrl" -}}
  {{- if .Values.customer.service.url -}}
    {{ .Values.service.customer.url }}
  {{- else -}}
    {{/* assume one is installed with release */}}
    {{- printf "http://%s-customer:8080" .Release.Name -}}
  {{- end }}
{{- end -}}

{{- define "authDockerImage" -}}
  {{- if .Values.global.useICPPrivateImages -}}
    {{/* assume image exists in ICP Private Registry */}}
    {{- printf "mycluster.icp:8500/default/bluecompute-auth" -}}
    {{/*{{- printf "mycluster.icp:8500/%s/bluecompute-auth" .Release.Namespace - */}}
  {{- else -}}
    {{- .Values.image.repository }}
  {{- end }}
{{- end -}}

{{- define "dataLoaderDockerImage" -}}
  {{- if .Values.global.useICPPrivateImages -}}
    {{/* assume image exists in ICP Private Registry */}}
    {{- printf "mycluster.icp:8500/default/bluecompute-dataloader" -}}
    {{/*- printf "mycluster.icp:8500/%s/bluecompute-dataloader" .Release.Namespace - */}}
  {{- else -}}
    {{- .Values.dataloader.image.repository }}
  {{- end }}
{{- end -}}