# rest-nvd

NVD RESTful API to serve CVE data

## Features

* Fetch NVD feeds and extract their content to individual static files (one per CVE)
* Serve CVE data through a REST API (`GET /vulnerabilities/CVE-<YEAR>-<NUMBER>`)
* Supports full sync (fetch all feeds from scratch) as well as incremental sync

## Synchronizing with the NVD

### Force full fetch (this is not necessary, not even the first run)

(this assumes the container is called `nvd_rest`)

`docker exec -ti nvd_rest bash -c "python /app/update.py --verbose --force"`

### Regular update

`docker exec -ti nvd_rest bash -c "python /app/update.py --verbose"`

This automatically detects if a full fetch is needed, or if an incremental update is enough.
This is all you need in most scenarios.

You might want to automate this with cron. The following line in your crontab will
do the update every 30 minutes. Once every hour would be ok too.

`*/30 *  *  *   *   /usr/bin/docker exec nvd_rest python /app/update.py --verbose >> /var/log/nvd_rest.log`

### Predicting license and language

We return this information when the service is called with the parameter `extended=true`.

```
Input: cve_id

if the URL has `extended=true`:
    if the file cve_id + "ext" (call it f) does not exist:
        ext_data = do_classification(cve_id)
        save ext_data to f
    else:
        ext_data = content of f
    return nvd data merged with ext_data
return data

```
