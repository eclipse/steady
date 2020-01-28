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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

/**
 * Helper class for deserialization of change-list.
 */
public class Bug {
    String bugId;
    String source;

    /**
     * <p>Constructor for Bug.</p>
     *
     * @param bugId a {@link java.lang.String} object.
     * @param source a {@link java.lang.String} object.
     */
    public Bug(String bugId, String source) {
        this.bugId = bugId;
        this.source = source;
    }

    /**
     * <p>Getter for the field <code>bugId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBugId() {
        return bugId;
    }

    /**
     * <p>Setter for the field <code>bugId</code>.</p>
     *
     * @param bugId a {@link java.lang.String} object.
     */
    public void setBugId(String bugId) {
        this.bugId = bugId;
    }

    /**
     * <p>Getter for the field <code>source</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSource() {
        return source;
    }

    /**
     * <p>Setter for the field <code>source</code>.</p>
     *
     * @param source a {@link java.lang.String} object.
     */
    public void setSource(String source) {
        this.source = source;
    }
    
    
}
