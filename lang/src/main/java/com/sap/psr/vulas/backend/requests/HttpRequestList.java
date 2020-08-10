/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.backend.requests;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.HttpResponse;
import com.sap.psr.vulas.goals.GoalContext;

/**
 * <p>HttpRequestList class.</p>
 *
 */
public class HttpRequestList extends AbstractHttpRequest {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    /**
     * When set to true, the sending of requests will be stopped upon success, i.e., once a Http response code 2xx will be received.
     */
    private boolean stopOnSuccess = true;

    private List<HttpRequest> list = new LinkedList<HttpRequest>();

    /**
     * <p>Constructor for HttpRequestList.</p>
     */
    public HttpRequestList() {
        this(true);
    }

    /**
     * <p>Constructor for HttpRequestList.</p>
     *
     * @param _stop_on_success a boolean.
     */
    public HttpRequestList(boolean _stop_on_success) {
        this.stopOnSuccess = _stop_on_success;
    }

    /**
     * <p>addRequest.</p>
     *
     * @param _r a {@link com.sap.psr.vulas.backend.requests.HttpRequest} object.
     */
    public void addRequest(HttpRequest _r) {
        this.list.add(_r);
    }

    /** {@inheritDoc} */
    @Override
    public HttpRequest setGoalContext(GoalContext _ctx) {
        this.context = _ctx;
        for (HttpRequest r : this.list) r.setGoalContext(_ctx);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * Loops over the list of requests and calls {@link HttpRequest#send()}. Depending on
     * the boolean {@link HttpRequestList#stopOnSuccess}, the sending stops or does not
     * stop in case of a successful call.
     */
    @Override
    public HttpResponse send() throws BackendConnectionException {
        HttpResponse response = null;
        for (HttpRequest r : this.list) {
            response = r.send();
            if (this.stopOnSuccess && response != null && (response.isOk() || response.isCreated()))
                break;
        }
        return response;
    }

    /** {@inheritDoc} */
    @Override
    public String getFilename() {
        String prefix = this.ms + "-hrl";
        return prefix;
    }

    /** {@inheritDoc} */
    @Override
    public void savePayloadToDisk() throws IOException {
        for (HttpRequest r : this.list) {
            r.savePayloadToDisk();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void loadPayloadFromDisk() throws IOException {
        for (HttpRequest r : this.list) {
            r.loadPayloadFromDisk();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deletePayloadFromDisk() throws IOException {
        for (HttpRequest r : this.list) {
            r.deletePayloadFromDisk();
        }
    }
}
