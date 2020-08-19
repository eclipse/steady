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
 * <p>NexusArtifact class.</p>
 *
 */
@XmlRootElement(name = "artifact")
public class NexusArtifact {

  String groupId;

  String artifactId;

  String version;

  /**
   * <p>Getter for the field <code>groupId</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * <p>Setter for the field <code>groupId</code>.</p>
   *
   * @param groupId a {@link java.lang.String} object.
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * <p>Getter for the field <code>artifactId</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * <p>Setter for the field <code>artifactId</code>.</p>
   *
   * @param artifact a {@link java.lang.String} object.
   */
  public void setArtifactId(String artifact) {
    this.artifactId = artifact;
  }

  /**
   * <p>Getter for the field <code>version</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVersion() {
    return version;
  }

  /**
   * <p>Setter for the field <code>version</code>.</p>
   *
   * @param version a {@link java.lang.String} object.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  // It does not look like this field is correctly populated
  // String latestRelease;

}
