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
package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>Tenant class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tenant implements Serializable {

    private String tenantToken = null;

    private String tenantName = null;

    private boolean isDefault = false;

    /**
     * <p>Constructor for Tenant.</p>
     */
    public Tenant() {
        this(null, null);
    }

    /**
     * <p>Constructor for Tenant.</p>
     *
     * @param _id a {@link java.lang.String} object.
     */
    public Tenant(String _id) {
        this(_id, null);
    }

    /**
     * <p>Constructor for Tenant.</p>
     *
     * @param _token a {@link java.lang.String} object.
     * @param _name a {@link java.lang.String} object.
     */
    public Tenant(String _token, String _name) {
        this.tenantToken = _token;
        this.tenantName = _name;
    }

    /**
     * <p>Getter for the field <code>tenantToken</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTenantToken() {
        return this.tenantToken;
    }
    /**
     * <p>Setter for the field <code>tenantToken</code>.</p>
     *
     * @param tenantToken a {@link java.lang.String} object.
     */
    public void setTenantToken(String tenantToken) {
        this.tenantToken = tenantToken;
    }

    /**
     * <p>Getter for the field <code>tenantName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTenantName() {
        return tenantName;
    }
    /**
     * <p>Setter for the field <code>tenantName</code>.</p>
     *
     * @param tenantName a {@link java.lang.String} object.
     */
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    /**
     * <p>toString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return this.tenantToken;
    }

    /**
     * <p>isDefault.</p>
     *
     * @return a boolean.
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * <p>setDefault.</p>
     *
     * @param isDefault a boolean.
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
