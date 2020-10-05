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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclipse.steady.patcheval.representation;

import org.eclipse.steady.shared.enums.ConstructChangeType;
import org.eclipse.steady.shared.json.model.ConstructId;

/**
 * class representing a consolidated change list
 * (no duplicates because of multiple commits on the same construct)
 */
public class OverallConstructChange {
  String fixedBody, buggyBody;
  ConstructChangeType changetype;
  String repoPath;
  ConstructId constructId;

  /**
   * <p>Constructor for OverallConstructChange.</p>
   *
   * @param fixedBody a {@link java.lang.String} object.
   * @param buggyBody a {@link java.lang.String} object.
   * @param changetype a {@link org.eclipse.steady.shared.enums.ConstructChangeType} object.
   * @param repoPath a {@link java.lang.String} object.
   * @param constructId a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public OverallConstructChange(
      String fixedBody,
      String buggyBody,
      ConstructChangeType changetype,
      String repoPath,
      ConstructId constructId) {
    this.fixedBody = fixedBody;
    this.buggyBody = buggyBody;
    this.changetype = changetype;
    this.repoPath = repoPath;
    this.constructId = constructId;
  }

  /**
   * <p>Getter for the field <code>fixedBody</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFixedBody() {
    return fixedBody;
  }

  /**
   * <p>Setter for the field <code>fixedBody</code>.</p>
   *
   * @param fixedBody a {@link java.lang.String} object.
   */
  public void setFixedBody(String fixedBody) {
    this.fixedBody = fixedBody;
  }

  /**
   * <p>Getter for the field <code>buggyBody</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getBuggyBody() {
    return buggyBody;
  }

  /**
   * <p>Setter for the field <code>buggyBody</code>.</p>
   *
   * @param buggyBody a {@link java.lang.String} object.
   */
  public void setBuggyBody(String buggyBody) {
    this.buggyBody = buggyBody;
  }

  /**
   * <p>getChangeType.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.ConstructChangeType} object.
   */
  public ConstructChangeType getChangeType() {
    return changetype;
  }

  /**
   * <p>setChangeType.</p>
   *
   * @param changetype a {@link org.eclipse.steady.shared.enums.ConstructChangeType} object.
   */
  public void setChangeType(ConstructChangeType changetype) {
    this.changetype = changetype;
  }

  /**
   * <p>Getter for the field <code>repoPath</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getRepoPath() {
    return repoPath;
  }

  /**
   * <p>Setter for the field <code>repoPath</code>.</p>
   *
   * @param repoPath a {@link java.lang.String} object.
   */
  public void setRepoPath(String repoPath) {
    this.repoPath = repoPath;
  }

  /**
   * <p>Getter for the field <code>constructId</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public ConstructId getConstructId() {
    return constructId;
  }

  /**
   * <p>Setter for the field <code>constructId</code>.</p>
   *
   * @param constructId a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   */
  public void setConstructId(ConstructId constructId) {
    this.constructId = constructId;
  }

  /**
   * <p>Getter for the field <code>changetype</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.ConstructChangeType} object.
   */
  public ConstructChangeType getChangetype() {
    return changetype;
  }

  /**
   * <p>Setter for the field <code>changetype</code>.</p>
   *
   * @param changetype a {@link org.eclipse.steady.shared.enums.ConstructChangeType} object.
   */
  public void setChangetype(ConstructChangeType changetype) {
    this.changetype = changetype;
  }
}
