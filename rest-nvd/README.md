# rest-nvd

`rest-nvd` is a service that provides a RESTful API to serve CVE data obtained
from the feeds that the NVD offers for download. This service is fast because
the json data corresponding to each vulnerability is kept in a separate file and
served without further processing.

This module is part of Eclipse Steady, but it can be run as a stand-alone service.

## Features

* Fetch NVD feeds and extract their content to individual static files (one per CVE).
* Serve CVE data through a REST API (`GET /vulnerabilities/CVE-<YEAR>-<NUMBER>`).
* Supports full sync (fetch all feeds from scratch) as well as periodic incremental sync.

## Synchronizing with the NVD

### Force full fetch

(this assumes the container is called `rest-nvd`)

`docker exec -ti rest_nvd bash -c "python /app/update.py --verbose --force"`

NOTE: the first time you run the container, the data folder and the metadata file that stores
the information about the last fetch are both absent, hence this forced update is triggered automatically.

### Regular update

`docker exec -ti rest_nvd bash -c "python /app/update.py --verbose"`

This compares the checksum of the last fetch with the one of the most recent feed available on the NVD.
Based on this comparison, the script determines if a full fetch is needed, if an incremental update is enough,
or if there is nothing new to fetch.

You might want to automate this with cron. The following line in your crontab will
do the update every 30 minutes. Once every hour would be ok too.

`*/30 *  *  *   *   /usr/bin/docker exec rest_nvd python /app/update.py --verbose >> /var/log/rest_nvd.log`