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
package com.sap.psr.vulas.cia.model.nexus;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>NexusArtifactInfoResourceResponse class.</p>
 *
 */
@XmlRootElement(name = "org.sonatype.nexus.rest.model.ArtifactInfoResourceResponse")
public class NexusArtifactInfoResourceResponse {

    NexusDescribeInfo data;

    /**
     * <p>Getter for the field <code>data</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.cia.model.nexus.NexusDescribeInfo} object.
     */
    public NexusDescribeInfo getData() {
        return data;
    }

    /**
     * <p>Setter for the field <code>data</code>.</p>
     *
     * @param data a {@link com.sap.psr.vulas.cia.model.nexus.NexusDescribeInfo} object.
     */
    public void setData(NexusDescribeInfo data) {
        this.data = data;
    }
}
