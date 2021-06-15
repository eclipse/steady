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
package org.eclipse.steady.kb.model;

import com.google.gson.annotations.SerializedName;

/**
 * Commit information
 */
public class Commit {
  private String timestamp;

  @SerializedName("commit_id")
  private String commitId;

  private String branch;
  ;

  @SerializedName("repository")
  private String repoUrl;

  private String directory;

  /**
   * <p>Getter for the field <code>timestamp</code>.</p>
   *
   * @return a {@link java.lang.String} object
   */
  public String getTimestamp() {
    return timestamp;
  }

  /**
   * <p>Setter for the field <code>timestamp</code>.</p>
   *
   * @param timestamp a {@link java.lang.String} object
   */
  public void setTimestamp(String timestamp) {
    if (timestamp == null) return;
    this.timestamp = timestamp.trim();
  }

  /**
   * <p>Getter for the field <code>commitId</code>.</p>
   *
   * @return a {@link java.lang.String} object
   */
  public String getCommitId() {
    return commitId;
  }

  /**
   * <p>Setter for the field <code>commitId</code>.</p>
   *
   * @param commitId a {@link java.lang.String} object
   */
  public void setCommitId(String commitId) {
    if (commitId == null) return;
    this.commitId = commitId.trim();
  }

  /**
   * <p>Getter for the field <code>branch</code>.</p>
   *
   * @return a {@link java.lang.String} object
   */
  public String getBranch() {
    return branch;
  }

  /**
   * <p>Setter for the field <code>branch</code>.</p>
   *
   * @param branch a {@link java.lang.String} object
   */
  public void setBranch(String branch) {
    if (branch == null) return;
    this.branch = branch.trim();
  }

  /**
   * <p>Getter for the field <code>repoUrl</code>.</p>
   *
   * @return a {@link java.lang.String} object
   */
  public String getRepoUrl() {
    return repoUrl;
  }

  /**
   * <p>Setter for the field <code>repoUrl</code>.</p>
   *
   * @param repoUrl a {@link java.lang.String} object
   */
  public void setRepoUrl(String repoUrl) {
    if (repoUrl == null) return;
    this.repoUrl = repoUrl.trim();
  }

  /**
   * <p>Getter for the field <code>directory</code>.</p>
   *
   * @return a {@link java.lang.String} object
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * <p>Setter for the field <code>directory</code>.</p>
   *
   * @param directory a {@link java.lang.String} object
   */
  public void setDirectory(String directory) {
    if (directory == null) return;
    this.directory = directory.trim();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Commit [timestamp="
        + timestamp
        + ", commitId="
        + commitId
        + ", branch="
        + branch
        + ", repoUrl="
        + repoUrl
        + ", directory="
        + directory
        + "]";
  }
}
