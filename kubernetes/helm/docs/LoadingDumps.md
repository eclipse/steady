# Loading dumps and migrating from existing database

This guide is destined to those who want to migrate their old Eclipse Steady database to the Kubernetes deployment.

## Preliminary

Requirements:
-   kubectl (with access to cluster)  

Warning:  
-   During the first phase, the source database will be under some strain so its recommended to perform this operation during low load periods
-   During the migration, accessing the destination database will break the destination database. Therefore, it is recommended to scale down the restbackend and restlibutils to avoid going into the broken state with these commands:
```sh
kubectl scale statefulset.apps restbackend --replicas 0 \
    && kubectl scale deployment.apps restlibutils --replicas 0
```
-   This process may take up to 5 hours depending on the amount of data present

## Migration using Go module

This migration can be automated using the utils packaged with this chart (which can be installed by following this [guide](../utils/README.md)). You can run the following command, once the module is built:

```sh
utils migrate -n { CoreNamespace } -sh { SourceHost } \
  -sp { SourcePort } -spa { Source Dump Path } -su { SourceUser } \
  -spw { SourcePassword } -dH { DestinationHost } -dp { DestinationPort } \
  -du { DestinationUser } -dpw { DestinationPassword }


# for more info and flags
utils migrate --help
```

## Manual Migration

This can be done with a single job run from within the destination cluster (provided the source is accessible from within the k8s cluster). This job is meant to be templated with helm with the command:
```sh
# with values.yaml
$ helm template . -x _postgres-migrator.yaml | kubectl apply -f -

# without values.yaml
$ helm template . -x _postgres-migrator.yaml \
  --set .... | kubectl apply -f -
```

It can however still be run without helm by manually replacing values inside {{ }} with the desired values and then applied via `kubectl apply -f -`.

### _postgres-migrator.yaml
```sh
apiVersion: batch/v1
kind: Job

metadata:
  name: postgres-migrator

spec:
  # Disables retries
  backoffLimit: 0

  template:
    spec:
      containers:
      - name: postgres-migrator
        # It is important that this variable corresponds to the source database version
        image: postgres:{{ .Values.migration.version }}
        command:
        - sh
        - -c
        - |-
          #!/bin/bash
          # env injection for pgdump
          export PGPASSWORD={{ .Values.migration.source.password }}

          DATE=`date +%Y%m%d`
          start_time=`date +%s`
          mkdir -p /dumps/

          # In values.yaml:
          # migration:
          #   source:
          #     port: # source database port to pull the dump from
          #     host: # source host
          #     dbname: # source vuln database name
          #     user: # user name
          #     password: # user password
          pg_dump -Fc --host {{ .Values.migration.source.host }} \
                  -U {{ .Values.migration.source.user }} \
                  --dbname {{ .Values.migration.source.dbname }} \
                  --port {{ .Values.migration.source.port }} >> /dumps/$DATE.dump

          echo "[+] Database dump completed in $((($(date +%s)-$start)/60)) minutes"
          start_time=`date +%s`

          # In values.yaml:
          # migration:
          #   destination:
          #     port: # source database port to pull the dump from
          #     host: # source host
          #     dbname: # source vuln database name
          #     user: # user name
          #     password: # user password
          export PGPASSWORD={{ .Values.migration.destination.password }}

          # If you have the chart up and running, you can
          # use the postgres-readwrite-service (master node)
          # as a host value directly
          pg_restore --verbose --clean --if-exists --no-acl \
                  --host {{ .Values.migration.destination.host }} \
                  --U {{ .Values.migration.destination.user }} \
                  --port {{ .Values.migration.destination.port }} \
                  --dbname {{ .Values.migration.destination.dbname }} \
                  --jobs={{ .Values.migration.cpu }}
                  /dumps/$DATE.dump

          echo "[+] Database restore completed in $((($(date +%s)-$start)/60)) minutes"

        restartPolicy: Never
        resources:
          # You can allocate more resources to increase restore speed
          # (the dump performance depends on your source database)
          requests:
            cpu: {{ .Values.migration.cpu }}
```

## Checking integrity

Due to the migration being often quite large, errors can occur that can break the database. Those can be identified by:

-   Tailing the logs of the migration job with `kubectl logs -f`. Some error may occur due to network issues, lack of space on the destination database, etc..
-   Trying to load the frontendapps or frontendbugs, you'll notice a huge performance dip (it could take up to 60s for simple queries instead of 5-6s max)
-   Accessing postgres and checking from inside the database with:
`kubectl exec -it postgres-master-0 psql -c 'SELECT pg_size_pretty( pg_database_size('dbname'))'`
