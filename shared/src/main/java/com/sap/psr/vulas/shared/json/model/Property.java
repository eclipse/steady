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
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.PropertySource;
import com.sap.psr.vulas.shared.util.DigestUtil;

/**
 * General purpose property referenced by all kinds of entities, e.g., {@link Library}s.
 * It is not dependent on (contained in) other entities but saved and created independently
 * in order to be able to have the same property referenced by multiple other entities.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
        ignoreUnknown = true,
        value = {"valueSha1"})
public class Property implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore private Long id;

    private PropertySource source;

    private String name;

    private String value;

    @JsonIgnore private String valueSha1;

    /**
     * <p>Constructor for Property.</p>
     */
    public Property() {
        super();
    }

    /**
     * <p>Constructor for Property.</p>
     *
     * @param source a {@link com.sap.psr.vulas.shared.enums.PropertySource} object.
     * @param _name a {@link java.lang.String} object.
     * @param _value a {@link java.lang.String} object.
     */
    public Property(PropertySource source, String _name, String _value) {
        super();
        this.source = source;
        this.name = _name;
        this.value = _value;
        this.valueSha1 =
                DigestUtil.getDigestAsString(
                        this.value, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getId() {
        return id;
    }
    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a {@link java.lang.Long} object.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * <p>Getter for the field <code>source</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.shared.enums.PropertySource} object.
     */
    public PropertySource getSource() {
        return source;
    }
    /**
     * <p>Setter for the field <code>source</code>.</p>
     *
     * @param source a {@link com.sap.psr.vulas.shared.enums.PropertySource} object.
     */
    public void setSource(PropertySource source) {
        this.source = source;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return name;
    }
    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param _name a {@link java.lang.String} object.
     */
    public void setName(String _name) {
        this.name = _name;
    }

    /**
     * <p>Getter for the field <code>value</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getValue() {
        return value;
    }
    /**
     * <p>Setter for the field <code>value</code>.</p>
     *
     * @param _value a {@link java.lang.String} object.
     */
    public void setValue(String _value) {
        this.value = _value;
        this.valueSha1 =
                DigestUtil.getDigestAsString(
                        this.value, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
    }

    /**
     * <p>Getter for the field <code>valueSha1</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getValueSha1() {
        return valueSha1;
    }
    /**
     * <p>Setter for the field <code>valueSha1</code>.</p>
     *
     * @param valueSha1 a {@link java.lang.String} object.
     */
    public void setValueSha1(String valueSha1) {
        this.valueSha1 = valueSha1;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((valueSha1 == null) ? 0 : valueSha1.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Property other = (Property) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (source != other.source) return false;
        if (valueSha1 == null) {
            if (other.valueSha1 != null) return false;
        } else if (!valueSha1.equals(other.valueSha1)) return false;
        return true;
    }
}
