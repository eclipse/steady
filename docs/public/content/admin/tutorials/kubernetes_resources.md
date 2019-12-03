
# Operational requirements

The following document is meant to summarize the vulnerability-assessment-tool requirements when it comes to running it on the Kubernetes architecture. This will detail its operational cost for a couple of scenarios:

-   **Lightweight** : the cluster is instantiated with no prior scan and data, then, the bugs are loaded using the patch-analyzer. This deployment is not destined for high availability or resilience (therefore with less replicas, no auto-scaling) and is optimal for small testing environments with a 6 month usage buffer.

-   **Lightweight HA** : same as the above but with the sufficient amount of replicas that will ensure high availability and resilience.

-   **Medium Load** : the cluster is instantiated with no prior scan and data, then, the bugs are loaded using the patch-analyzer. This deployment is not destined for high availability or resilience (therefore with less replicas, no auto-scaling) and is optimal for small production environments with a 2 year buffer.

-   **Medium Load HA** : same as the above but with the sufficient amount of replicas that will ensure high availability and resilience.

-   **Production Load** : the cluster is loaded with the latest dump of the internal SAP vulnerability-assessment-tool database (which at the time of this document creation is around 249GB). This deployment is not destined for high availability or
resilience and is optimal for production environments with a 3-5 year usage buffer. This data load includes app specific data (once those are removed, the database size is around 150GB in our current setup)

-   **Production Load HA** : same as the above but with the sufficient amount of replicas that will ensure high availability and resilience.

-   **Extra PVC** : this use case is destined towards optimizing certain components of the vulnerability-assessment-tool which require read write many volumes (in particular `rest-lib-utils`)

-   **Hosted DB** : for using a pre-existing database (for cloud providers such as GCP, AWS, Azure, etc...) which require lower resources as the database are no longer self managed.

| | CPU request | CPU Limit | Memory Request (GiB) | Memory Limit (GiB) | PV (GiB) |
| ----------------------------------- | ----------- | --------- | -------------------- | ------------------ | -------- |
| Lightweight | 8.5 | 17.1 | 12.5 | 25.6 | 30 |
| Lightweight HA | 17.6 | 35.2 | 30.5 | 61.2 | 90 |
| Lightweight HA (with extra PVC) | 17.6 | 35.2 | 30.5 | 61.2 | 102 |
| Medium Load | 16.5 | 33.1 | 24.5 | 49.6 | 50 |
| Medium Load HA | 30.2 | 60.7 | 51.7 | 102.7 | 150 |
| Medium Load HA (with extra PVC) | 30.2 | 60.7 | 51.7 | 102.7 | 195 |
| Production Load | 40.7 | 57.6 | 57.2 | 92.6 | 400 |
| Production Load HA | 107.9 | 160.2 | 159.4 | 262.2 | 1200 |
| Production Load HA (with extra PVC) | 107.9 | 160.2 | 159.4 | 262.2 | 1380 |
| Lightweight HA (hosted db) | 8.7 | 17.7 | 16.7 | 34.2 | 90 |
| Medium Load HA (hosted db) | 16.7 | 33.7 | 32.7 | 66.2 | 150 |
| Production Load HA (hosted db) | 48.9 | 98.2 | 97.4 | 197.2 | 1200 |


### In-depth break down of resource requirements

#### Frontendapps

|                          | replicas | cpu_limit | cpu_request | memory_limit(GiB) | memory_request(GiB) | PV(GiB) |
|--------------------------|----------|-----------|-------------|-------------------|---------------------|---------|
| lightweight              | 1        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| lightweight_ha           | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| lightweight_ha_extra_pvc | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| medium_load              | 1        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| medium_load_ha           | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| medium_load_ha_extra_pvc | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| prod_load                | 1        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| prod_load_ha             | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| prod_load_ha_extra_pvc   | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| lightweight_ha_hosted_db | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| medium_load_ha_hosted_db | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| prod_load_ha_hosted_db   | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |


#### Frontendbugs

|                          | replicas | cpu_limit | cpu_request | memory_limit(GiB) | memory_request(GiB) | PV(GiB) |
|--------------------------|----------|-----------|-------------|-------------------|---------------------|---------|
| lightweight              | 1        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| lightweight_ha           | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| lightweight_ha_extra_pvc | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| medium_load              | 1        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| medium_load_ha           | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| medium_load_ha_extra_pvc | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| prod_load                | 1        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| prod_load_ha             | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| prod_load_ha_extra_pvc   | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| lightweight_ha_hosted_db | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| medium_load_ha_hosted_db | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |
| prod_load_ha_hosted_db   | 2        | 0.1       | 0.3         | 0.1               | 0.3                 | 0       |

#### Patch-lib-analyzer

|                          | replicas | cpu_limit | cpu_request | memory_limit(GiB) | memory_request(GiB) | PV(GiB) |
|--------------------------|----------|-----------|-------------|-------------------|---------------------|---------|
| lightweight              | 1        | 0.3       | 0.5         | 0.3               | 1                   | 0       |
| lightweight_ha           | 2        | 0.3       | 0.5         | 0.3               | 1                   | 0       |
| lightweight_ha_extra_pvc | 2        | 0.3       | 0.5         | 0.3               | 1                   | 2       |
| medium_load              | 1        | 0.3       | 0.5         | 0.3               | 1                   | 0       |
| medium_load_ha           | 2        | 0.3       | 0.5         | 0.3               | 1                   | 0       |
| medium_load_ha_extra_pvc | 2        | 0.3       | 0.5         | 0.3               | 1                   | 5       |
| prod_load                | 1        | 0.5       | 1           | 1                 | 4                   | 0       |
| prod_load_ha             | 2        | 0.5       | 1           | 1                 | 4                   | 30      |
| prod_load_ha_extra_pvc   | 2        | 0.5       | 1           | 1                 | 4                   | 0       |
| lightweight_ha_hosted_db | 2        | 0.3       | 0.5         | 0.3               | 1                   | 0       |
| medium_load_ha_hosted_db | 2        | 0.3       | 0.5         | 0.3               | 1                   | 0       |
| prod_load_ha_hosted_db   | 2        | 0.5       | 1           | 1                 | 0.3                 | 0       |

#### Rest-lib-utils

|                          | replicas | cpu_limit | cpu_request | memory_limit(GiB) | memory_request(GiB) | PV(GiB) |
|--------------------------|----------|-----------|-------------|-------------------|---------------------|---------|
| lightweight              | 1        | 2         | 4           | 4                 | 8                   | 0       |
| lightweight_ha           | 2        | 2         | 4           | 4                 | 8                   | 0       |
| lightweight_ha_extra_pvc | 2        | 2         | 4           | 4                 | 8                   | 5       |
| medium_load              | 1        | 4         | 8           | 8                 | 16                  | 0       |
| medium_load_ha           | 2        | 4         | 8           | 8                 | 16                  | 0       |
| medium_load_ha_extra_pvc | 2        | 4         | 8           | 8                 | 16                  | 20      |
| prod_load                | 1        | 8         | 16          | 16                | 32                  | 0       |
| prod_load_ha             | 3        | 8         | 16          | 16                | 32                  | 0       |
| prod_load_ha_extra_pvc   | 3        | 8         | 16          | 16                | 32                  | 50      |
| lightweight_ha_hosted_db | 2        | 2         | 4           | 4                 | 8                   | 0       |
| medium_load_ha_hosted_db | 2        | 4         | 8           | 8                 | 16                  | 0       |
| prod_load_ha_hosted_db   | 3        | 8         | 16          | 16                | 32                  | 0       |

#### Rest-backend

|                          | replicas | cpu_limit | cpu_request | memory_limit(GiB) | memory_request(GiB) | PV(GiB) |
|--------------------------|----------|-----------|-------------|-------------------|---------------------|---------|
| lightweight              | 1        | 2         | 4           | 4                 | 8                   | 0       |
| lightweight_ha           | 2        | 2         | 4           | 4                 | 8                   | 0       |
| lightweight_ha_extra_pvc | 2        | 2         | 4           | 4                 | 8                   | 0       |
| medium_load              | 1        | 4         | 8           | 8                 | 16                  | 0       |
| medium_load_ha           | 2        | 4         | 8           | 8                 | 16                  | 0       |
| medium_load_ha_extra_pvc | 2        | 4         | 8           | 8                 | 16                  | 0       |
| prod_load                | 1        | 8         | 16          | 16                | 32                  | 0       |
| prod_load_ha             | 3        | 8         | 16          | 16                | 32                  | 0       |
| prod_load_ha_extra_pvc   | 3        | 8         | 16          | 16                | 32                  | 0       |
| lightweight_ha_hosted_db | 2        | 2         | 4           | 4                 | 8                   | 0       |
| medium_load_ha_hosted_db | 2        | 4         | 8           | 8                 | 16                  | 0       |
| prod_load_ha_hosted_db   | 3        | 8         | 16          | 16                | 32                  | 0       |

#### Postgres master

|                          | replicas | cpu_limit | cpu_request | memory_limit(GiB) | memory_request(GiB) | PV(GiB) |
|--------------------------|----------|-----------|-------------|-------------------|---------------------|---------|
| lightweight              | 1        | 4         | 8           | 4                 | 8                   | 30      |
| lightweight_ha           | 1        | 4         | 8           | 4                 | 8                   | 0       |
| lightweight_ha_extra_pvc | 1        | 4         | 8           | 4                 | 8                   | 0       |
| medium_load              | 1        | 8         | 16          | 8                 | 16                  | 50      |
| medium_load_ha           | 1        | 8         | 16          | 8                 | 16                  | 50      |
| medium_load_ha_extra_pvc | 1        | 8         | 16          | 8                 | 16                  | 50      |
| prod_load                | 1        | 24        | 24          | 24                | 24                  | 0       |
| prod_load_ha             | 1        | 24        | 24          | 24                | 24                  | 0       |
| prod_load_ha_extra_pvc   | 1        | 24        | 24          | 24                | 24                  | 0       |
| lightweight_ha_hosted_db | 1        | 0         | 0           | 0                 | 0                   | 30      |
| medium_load_ha_hosted_db | 1        | 0         | 0           | 0                 | 0                   | 50      |
| prod_load_ha_hosted_db   | 1        | 0         | 0           | 0                 | 0                   | 400     |

#### Postgres replicas

|                          | replicas | cpu_limit | cpu_request | memory_limit(GiB) | memory_request(GiB) | PV(GiB) |
|--------------------------|----------|-----------|-------------|-------------------|---------------------|---------|
| lightweight              | 0        | 0         | 0           | 0                 | 0                   | 0       |
| lightweight_ha           | 2        | 2         | 4           | 4                 | 8                   | 30      |
| lightweight_ha_extra_pvc | 2        | 2         | 4           | 4                 | 8                   | 0       |
| medium_load              | 0        | 0         | 0           | 0                 | 0                   | 0       |
| medium_load_ha           | 2        | 2         | 4           | 4                 | 8                   | 50      |
| medium_load_ha_extra_pvc | 2        | 2         | 4           | 4                 | 8                   | 50      |
| prod_load                | 0        | 0         | 0           | 0                 | 0                   | 0       |
| prod_load_ha             | 2        | 16        | 16          | 16                | 16                  | 400     |
| prod_load_ha_extra_pvc   | 2        | 16        | 16          | 16                | 16                  | 400     |
| lightweight_ha_hosted_db | 2        | 0         | 0           | 0                 | 0                   | 30      |
| medium_load_ha_hosted_db | 2        | 0         | 0           | 0                 | 0                   | 50      |
| prod_load_ha_hosted_db   | 2        | 0         | 0           | 0                 | 0                   | 400     |

#### Pgpool

|                          | replicas | cpu_limit | cpu_request | memory_limit(GiB) | memory_request(GiB) | PV(GiB) |
|--------------------------|----------|-----------|-------------|-------------------|---------------------|---------|
| lightweight              | 0        | 0         | 0           | 0                 | 0                   | 0       |
| lightweight_ha           | 3        | 0.3       | 0.5         | 0.6               | 1                   | 0       |
| lightweight_ha_extra_pvc | 3        | 0.3       | 0.5         | 0.6               | 1                   | 0       |
| medium_load              | 0        | 0         | 0           | 0                 | 0                   | 0       |
| medium_load_ha           | 3        | 0.5       | 1           | 1                 | 1.5                 | 0       |
| medium_load_ha_extra_pvc | 3        | 0.5       | 1           | 1                 | 1.5                 | 0       |
| prod_load                | 0        | 0         | 0           | 0                 | 0                   | 0       |
| prod_load_ha             | 3        | 1         | 2           | 2                 | 3                   | 0       |
| prod_load_ha_extra_pvc   | 3        | 1         | 2           | 2                 | 3                   | 0       |
| lightweight_ha_hosted_db | 0        | 0         | 0           | 0                 | 0                   | 0       |
| medium_load_ha_hosted_db | 0        | 0         | 0           | 0                 | 0                   | 0       |
| prod_load_ha_hosted_db   | 0        | 0         | 0           | 0                 | 0                   | 0       |
