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
 * <p>NexusDescribeInfo class.</p>
 */
public class NexusDescribeInfo {
  Long uploaded;
  Long lastChanged;
  String sha1Hash;

  /**
   * <p>Getter for the field <code>uploaded</code>.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getUploaded() {
    return uploaded;
  }
  /**
   * <p>Setter for the field <code>uploaded</code>.</p>
   *
   * @param uploaded a {@link java.lang.Long} object.
   */
  public void setUploaded(Long uploaded) {
    this.uploaded = uploaded;
  }
  /**
   * <p>Getter for the field <code>lastChanged</code>.</p>
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getLastChanged() {
    return lastChanged;
  }
  /**
   * <p>Setter for the field <code>lastChanged</code>.</p>
   *
   * @param lastChanged a {@link java.lang.Long} object.
   */
  public void setLastChanged(Long lastChanged) {
    this.lastChanged = lastChanged;
  }
  /**
   * <p>Getter for the field <code>sha1Hash</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSha1Hash() {
    return sha1Hash;
  }
  /**
   * <p>Setter for the field <code>sha1Hash</code>.</p>
   *
   * @param sha1Hash a {@link java.lang.String} object.
   */
  public void setSha1Hash(String sha1Hash) {
    this.sha1Hash = sha1Hash;
  }
}
