#
# This file is part of Eclipse Steady.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
#

from flask import Flask
from flask import request
from flask import jsonify

import json
import os
import logging

app = Flask(__name__)

# Setup gunicorn logging
#
# see https://medium.com/@trstringer/logging-flask-and-gunicorn-the-manageable-way-2e6f0b8beb2f
# for an explanation of how this works
if __name__ != '__main__':
    gunicorn_logger = logging.getLogger('gunicorn.error')
    app.logger.handlers = gunicorn_logger.handlers
    app.logger.setLevel(gunicorn_logger.level)

#ENABLE_DASHBOARD=bool(os.environ.get('DASHBOARD')) or False

## monitoring dashboard
#if ENABLE_DASHBOARD:
#    app.logger.debug('Monitoring dashboard is enabled')
#    import flask_monitoringdashboard as dashboard
#    dashboard.bind(app)
#else:
#    app.logger.debug('Monitoring dashboard is disabled')

DATA_PATH=os.environ.get('CVE_DATA_PATH') or os.path.join(os.path.realpath(os.path.dirname(__file__)), '..', 'data')
PREFIX_URL = "/nvd"

@app.route(PREFIX_URL + '/vulnerabilities/<vuln_id>')
def get_vuln_data(vuln_id):
    extended_data = request.args.get('extended', default = False, type = bool)
    app.logger.debug('Requested data for vulnerability ' + vuln_id )

    year = vuln_id.split('-')[1]
    json_file = os.path.join(DATA_PATH, year, vuln_id.upper()+'.json')
    if not os.path.isfile(json_file):
        app.logger.info('No file found: ' + json_file)
        return '', 404

    app.logger.debug('Serving file: ' + json_file)
    with open(json_file) as f:
        data = json.loads(f.read())

    if extended_data:
        lang = predict_language(data)
        lic = predict_license(data)

        data = jsonify({'extended_data': {'license': lic, 'language': lang}, 'nvd_data': data})

    return data, 200


@app.route(PREFIX_URL + '/status')
def status():
    app.logger.debug('Serving status page')
    out = dict()
    metadata_file = os.path.join(DATA_PATH, 'metadata.json')
    if os.path.isfile(metadata_file):
        with open(metadata_file) as f:
            metadata = json.loads(f.read())
        out['metadata'] = metadata
        return json.dumps(out), 200, {'Content-Type': 'application/json; charset=utf-8'}
    return '', 404


@app.route('/')
@app.route(PREFIX_URL + '/')
def index():
    app.logger.debug('Serving root resource')
    return "try with /nvd/vulnerabilities/<VULN_ID>"

def predict_language(data):
    return { "prediction": "Java", "confidence" : 0.98 }

def predict_license(data):
    return { "prediction": "OSS", "confidence" : 0.76 }

if __name__ == '__main__':
    app.run()
