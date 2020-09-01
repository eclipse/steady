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
package org.eclipse.steady.malice;

/**
 * <p>MaliciousnessAnalysisResult class.</p>
 *
 */
public class MaliciousnessAnalysisResult {

  private double result = 0;

  private String reason = null;

  private String analyzer = null;

  /**
   * <p>Getter for the field <code>result</code>.</p>
   *
   * @return a double.
   */
  public double getResult() {
    return result;
  }

  /**
   * <p>Setter for the field <code>result</code>.</p>
   *
   * @param result a double.
   */
  public void setResult(double result) {
    this.result = result;
  }

  /**
   * <p>isBenign.</p>
   *
   * @return a boolean.
   */
  public boolean isBenign() {
    return result == 0d;
  }

  /**
   * <p>isMalicious.</p>
   *
   * @return a boolean.
   */
  public boolean isMalicious() {
    return result > 0d;
  }

  /**
   * <p>Getter for the field <code>reason</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getReason() {
    return reason;
  }

  /**
   * <p>Setter for the field <code>reason</code>.</p>
   *
   * @param _reason a {@link java.lang.String} object.
   */
  public void setReason(String _reason) {
    this.reason = _reason;
  }

  /**
   * <p>appendReason.</p>
   *
   * @param _reason a {@link java.lang.String} object.
   * @param _separator a {@link java.lang.String} object.
   */
  public void appendReason(String _reason, String _separator) {
    this.reason = (this.reason == null ? "" : this.reason + _separator) + _reason;
  }

  /**
   * <p>Getter for the field <code>analyzer</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAnalyzer() {
    return analyzer;
  }

  /**
   * <p>Setter for the field <code>analyzer</code>.</p>
   *
   * @param analyzer a {@link java.lang.String} object.
   */
  public void setAnalyzer(String analyzer) {
    this.analyzer = analyzer;
  }
}
