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
package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The result of a search for {@link ConstructId}s in all {@link Dependency}s of an {@link Application}.
 * TODO (HP, 7.3.2017): Check whether it is worthwhile to also create a class ConstructSearch. Right now, all of that is handled in the controller method.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstructSearchResult implements Comparable {

    private ConstructId constructId = null;

    private Dependency dependency = null;

    /**
     * <p>Constructor for ConstructSearchResult.</p>
     *
     * @param _d a {@link com.sap.psr.vulas.backend.model.Dependency} object.
     * @param _cid a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
     */
    public ConstructSearchResult(Dependency _d, ConstructId _cid) {
        this.constructId = _cid;
        this.dependency = _d;
    }

    /**
     * <p>Getter for the field <code>constructId</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
     */
    public ConstructId getConstructId() {
        return constructId;
    }

    /**
     * <p>Setter for the field <code>constructId</code>.</p>
     *
     * @param constructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
     */
    public void setConstructId(ConstructId constructId) {
        this.constructId = constructId;
    }

    /**
     * <p>Getter for the field <code>dependency</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.backend.model.Dependency} object.
     */
    public Dependency getDependency() {
        return dependency;
    }

    /**
     * <p>Setter for the field <code>dependency</code>.</p>
     *
     * @param dependency a {@link com.sap.psr.vulas.backend.model.Dependency} object.
     */
    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((constructId == null) ? 0 : constructId.hashCode());
        result = prime * result + ((dependency == null) ? 0 : dependency.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ConstructSearchResult other = (ConstructSearchResult) obj;
        if (constructId == null) {
            if (other.constructId != null) return false;
        } else if (!constructId.equals(other.constructId)) return false;
        if (dependency == null) {
            if (other.dependency != null) return false;
        } else if (!dependency.equals(other.dependency)) return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Object _o) {
        if (!(_o instanceof ConstructSearchResult))
            throw new IllegalArgumentException("Wrong argument type: " + _o.getClass().getName());

        ConstructSearchResult other = (ConstructSearchResult) _o;
        int c = this.getConstructId().compareTo(other.getConstructId());
        if (c == 0)
            c = this.getDependency().getFilename().compareTo(other.getDependency().getFilename());
        return c;
    }
}
