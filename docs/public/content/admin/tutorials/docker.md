# Deploy on Docker

In this tutorial you will be guided through the necessary steps to set-up the @@PROJECT_NAME@@ backend services.

<center class='expandable'>
    [![start_page](../img/components.png)](../img/components.png)
</center>

!!! warning "Important Remark"

    The setup obtained following these instructions is meant for demonstration purposes.
    It **shall not** be used in productive scenarios (both for security and scalability concerns).
    <!--Detailed instructions on how to operate @@PROJECT_NAME@@ in a productive environment are given in the [Admin Guide](admin_guide.md).-->

## Pre-requisites

- git
- docker
- docker-compose

## Installation

### Setup

Clone locally the `vulnerability-assessment-tool` repository

```sh
git clone https://github.com/SAP/vulnerability-assessment-tool
```

Customize the file `docker/.env` to match your needs, make sure you set the version you want to run in VULAS_RELEASE.

```sh
cp docker/.env.sample docker/.env
```

> In `docker/.env` you must configure at least `POSTGRES_USER=`, you should also configure the `HAPROXY`'s user and password as well as the credentials to access the bugs' frontend

### Run

You are now ready to run the system:

```sh
(cd docker && docker-compose up -d --build)
```

To check if everything started successfully, check the page `http://localhost:8033/haproxy?stats`. All endpoints should appear as green (you may want to replace `localhost` with the actual hostname of your machine).

!!! info "Credentials and start up time"
    `username` and `password` can be found in your `.env` file, be also advised that `rest-backend` could take more than 30 seconds to be available to answer HTTP requests

### Populate/maintain the vulnerability database

In order for the tool to detect vulnerabilities, you need to import and analyze them first so that they are available in the tool's vulnerability database. Large part of CVE's and bugs are open sourced in [vulnerability-assessment-kb](https://github.com/SAP/vulnerability-assessment-kb).

Follow the instructions mentioned [here](../../../vuln_db/tutorials/vuln_db_tutorial/#batch-import-from-knowledge-base), to import and build all the vulnerabilities' knowledge.

---

Get going:

1. [Import](../../../vuln_db/tutorials/vuln_db_tutorial/) all the CVEs and bugs in your local database
2. Setup your [workspace](../../../user/manuals/setup/#workspace) (if you don't have one)
3. Become familiar with the various analysis [goals](../../../user/manuals/analysis/) (first time users)
4. Analyze your [Java](../../../user/tutorials/java_maven/) or [Python](../../../user/tutorials/python_cli/) application (on a regular basis)
5. [Assess](../../../user/manuals/assess_and_mitigate/) findings using the apps Web frontend (following every analysis)

Further links:

- [Configure](../../../user/tutorials/) the client-side analysis
- [Automate](../../../user/tutorials/jenkins_howto/) with Jenkins
- [Get help](../../../user/support/) if you run into troubles
