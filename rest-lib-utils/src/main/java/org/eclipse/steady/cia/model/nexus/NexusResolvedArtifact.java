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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.cia.model.nexus;

/**
 * <p>NexusResolvedArtifact class.</p>
 *
 */
public class NexusResolvedArtifact {

  // @XmlAttribute(name="groupId")
  String groupId;

  //	@XmlAttribute(name="artifactId")
  String artifactId;

  // @XmlAttribute(name="version")
  String version;

  // @XmlAttribute(name="extension")
  String extension;

  // @XmlAttribute(name="snapshot")
  String snapshot;

  // @XmlAttribute(name="snapshotBuildNumber")
  String snapshotBuildNumber;

  // @XmlAttribute(name="snapshotTimeStamp")
  String snapshotTimeStamp;

  // @XmlAttribute(name="sha1")
  String sha1;

  // @XmlAttribute(name="repositoryPath")
  String repositoryPath;

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
   * @param artifactId a {@link java.lang.String} object.
   */
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
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

  /**
   * <p>Getter for the field <code>extension</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * <p>Setter for the field <code>extension</code>.</p>
   *
   * @param extension a {@link java.lang.String} object.
   */
  public void setExtension(String extension) {
    this.extension = extension;
  }

  /**
   * <p>Getter for the field <code>snapshot</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSnapshot() {
    return snapshot;
  }

  /**
   * <p>Setter for the field <code>snapshot</code>.</p>
   *
   * @param snapshot a {@link java.lang.String} object.
   */
  public void setSnapshot(String snapshot) {
    this.snapshot = snapshot;
  }

  /**
   * <p>Getter for the field <code>snapshotBuildNumber</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSnapshotBuildNumber() {
    return snapshotBuildNumber;
  }

  /**
   * <p>Setter for the field <code>snapshotBuildNumber</code>.</p>
   *
   * @param snapshotBuildNumber a {@link java.lang.String} object.
   */
  public void setSnapshotBuildNumber(String snapshotBuildNumber) {
    this.snapshotBuildNumber = snapshotBuildNumber;
  }

  /**
   * <p>Getter for the field <code>snapshotTimeStamp</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSnapshotTimeStamp() {
    return snapshotTimeStamp;
  }

  /**
   * <p>Setter for the field <code>snapshotTimeStamp</code>.</p>
   *
   * @param snapshotTimeStamp a {@link java.lang.String} object.
   */
  public void setSnapshotTimeStamp(String snapshotTimeStamp) {
    this.snapshotTimeStamp = snapshotTimeStamp;
  }

  /**
   * <p>Getter for the field <code>sha1</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSha1() {
    return sha1;
  }

  /**
   * <p>Setter for the field <code>sha1</code>.</p>
   *
   * @param sha1 a {@link java.lang.String} object.
   */
  public void setSha1(String sha1) {
    this.sha1 = sha1;
  }

  /**
   * <p>Getter for the field <code>repositoryPath</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepositoryPath() {
    return repositoryPath;
  }

  /**
   * <p>Setter for the field <code>repositoryPath</code>.</p>
   *
   * @param repositoryPath a {@link java.lang.String} object.
   */
  public void setRepositoryPath(String repositoryPath) {
    this.repositoryPath = repositoryPath;
  }
}
