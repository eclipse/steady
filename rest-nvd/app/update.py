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

import requests
import zipfile
from contextlib import closing
import io
import os
import sys
import json
import time
import plac
from tqdm import tqdm
from io import StringIO
from pprint import pprint
import logging

# note: The NVD has not data older than 2002
START_FROM_YEAR=os.environ.get('CVE_DATA_AS_OF_YEAR') or '2002'
DATA_PATH=os.environ.get('CVE_DATA_PATH') or 'data/'
FEED_SCHEMA_VERSION=os.environ.get('FEED_SCHEMA_VERSION') or '1.1'

def do_update(verbose=False):
    # read metadata of last fetch
    last_fetch_metadata = dict()
    try:
        with open(os.path.join(DATA_PATH, 'metadata.json'), 'r') as f:
            last_fetch_metadata = json.load(f)
            print('[ii] last fetch: ' + last_fetch_metadata['sha256'])
    except:
        last_fetch_metadata['sha256'] = ''
        print('[ii] Could not read metadata about previous fetches (this might be the first time we fetch data).')


    # read metadata of new data from the NVD site
    url = 'https://nvd.nist.gov/feeds/json/cve/{}/nvdcve-{}-modified.meta'.format(FEED_SCHEMA_VERSION, FEED_SCHEMA_VERSION)
    r = requests.get(url)
    if r.status_code != 200:
        print('[!!] Received status code {} when contacting {}.'.format(r.status_code, url))
        return False

    metadata_txt = r.text.strip().split('\n')
    metadata_dict = dict()
    for d in metadata_txt:
        d_split = d.split(':',1)
        metadata_dict[d_split[0]] = d_split[1].strip()
    print('[ii] current:    ' + metadata_dict['sha256'])

    # check if the new data is actually new
    if last_fetch_metadata['sha256'] == metadata_dict['sha256']:
        print('[ii] We already have this update, no new data to fetch.')
        return False
    else:
        do_fetch('modified')
        with open(os.path.join(DATA_PATH, 'metadata.json'), 'w') as f:
            f.write(json.dumps(metadata_dict))
        return True

def do_fetch_full(start_from_year=START_FROM_YEAR, verbose=False):
    years_to_fetch = [ y for y in range(int(START_FROM_YEAR), int(time.strftime("%Y"))+1 ) ]
    if verbose:
        print('[ii] Fetching feeds: ' + str(years_to_fetch))

    for y in years_to_fetch:
        if not do_fetch(y):
            print("[!!] Could not fetch data for year " + str(y))


def do_fetch(what,verbose=False):
    '''
    the 'what' parameter can be a year or 'recent' or 'modified'
    '''
    url = 'https://nvd.nist.gov/feeds/json/cve/{}/nvdcve-{}-{}.json.zip'.format(FEED_SCHEMA_VERSION, FEED_SCHEMA_VERSION, what)
    r = requests.get(url)
    if r.status_code != 200:
        print('[!!] Received status code {} when contacting {}.'.format(r.status_code, url))
        return False

    with closing(r), zipfile.ZipFile(io.BytesIO(r.content)) as archive:
        for f in archive.infolist():
            print(f.filename)
            data = json.loads(archive.read(f).decode())

    pbar = tqdm(data['CVE_Items'])
    for v in pbar:
        CVE_id = v['cve']['CVE_data_meta']['ID']
        CVE_year = CVE_id.split('-')[1]
        target_dir = os.path.join(DATA_PATH,  CVE_year)
        if not os.path.isdir(target_dir):
            # pbar.set_description('Create dir ' + target_dir)
            os.makedirs(target_dir)

        with open(os.path.join(target_dir, CVE_id + '.json'), 'w') as f:
            # pbar.set_description('Updating: ' + CVE_id)
            f.write(json.dumps(v))

    return True

def need_full():
    if os.path.exists(DATA_PATH) and os.path.isdir(DATA_PATH):
        if not os.listdir(DATA_PATH):
            print('[ii] Data folder is empty')
            return True
        else:
            # Directory exists and is not empty
            print('[ii] Data folder found')
            return False
    else:
        # Directory doesn't exist
        print('[ii] Data folder is missing')
        return True

@plac.annotations(
    force=("Force a full update of all feeds", 'flag', 'f', bool),
    verbose=("Verbose mode", 'flag', 'v', bool)
)
def main(force=False, verbose=False):

    if force or need_full():
        do_fetch_full(verbose=verbose)

    # always do this, so that metadata are fine and so is the /status API
    do_update(verbose=verbose)

if __name__ == "__main__":
    plac.call(main)
