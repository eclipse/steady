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

/**
 * Information about the affectedness of artifacts contained in a statement.yaml file.
 */
public class Artifact {
  private String id;
  private Boolean affected;
  private String reason;

  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link java.lang.String} object
   */
  public String getId() {
    return id;
  }

  /**
   * <p>Setter for the field <code>id</code>.</p>
   *
   * @param id a {@link java.lang.String} object
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * <p>Getter for the field <code>affected</code>.</p>
   *
   * @return a {@link java.lang.Boolean} object
   */
  public Boolean getAffected() {
    return affected;
  }

  /**
   * <p>Setter for the field <code>affected</code>.</p>
   *
   * @param affected a {@link java.lang.Boolean} object
   */
  public void setAffected(Boolean affected) {
    this.affected = affected;
  }

  /**
   * <p>Getter for the field <code>reason</code>.</p>
   *
   * @return a {@link java.lang.String} object
   */
  public String getReason() {
    return reason;
  }

  /**
   * <p>Setter for the field <code>reason</code>.</p>
   *
   * @param reason a {@link java.lang.String} object
   */
  public void setReason(String reason) {
    this.reason = reason;
  }
}
