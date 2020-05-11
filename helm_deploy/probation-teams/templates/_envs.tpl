    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "postgres"

  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: OAUTH_ENDPOINT_URL
    value: "{{ .Values.env.OAUTH_ENDPOINT_URL }}"

  - name: SUPERUSER_USERNAME
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: database_username

  - name: SUPERUSER_PASSWORD
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: database_password

  - name: DATABASE_USERNAME
    value: "probation_teams"

  - name: DATABASE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: probation_teams_password

  - name: DATABASE_NAME
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: database_name

  - name: DATABASE_ENDPOINT
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: rds_instance_endpoint

{{- end -}}
