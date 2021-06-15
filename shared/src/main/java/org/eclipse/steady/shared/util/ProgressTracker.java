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
package org.eclipse.steady.shared.util;

/**
 * <p>ProgressTracker class.</p>
 */
public class ProgressTracker {

  private double total = -1;
  private double current = 0;

  /**
   * <p>Constructor for ProgressTracker.</p>
   *
   * @param _total a double.
   */
  public ProgressTracker(double _total) {
    this.total = _total;
  }

  /**
   * Returns the increase in percent of the total.
   *
   * @param _by a double.
   * @throws java.lang.IllegalArgumentException
   * @return a double.
   */
  public double increase(double _by) throws IllegalArgumentException {
    if (this.current + _by > this.total) throw new IllegalArgumentException("Total exceeded");
    this.current += _by;
    return _by / this.total;
  }

  /**
   * <p>Getter for the field <code>current</code>.</p>
   *
   * @return a double.
   */
  public double getCurrent() {
    return this.current;
  }

  /**
   * <p>Getter for the field <code>total</code>.</p>
   *
   * @return a double.
   */
  public double getTotal() {
    return this.total;
  }

  /*
   * Returns the completion in percent.
   */
  /**
   * <p>getCompletion.</p>
   *
   * @return a double.
   */
  public double getCompletion() {
    return 100 * this.current / this.total;
  }

  /*
   * Returns the completion in percent.
   */
  /**
   * <p>getCompletionAsLong.</p>
   *
   * @return a long.
   */
  public long getCompletionAsLong() {
    return (long) Math.floor(this.getCompletion());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final StringBuffer b = new StringBuffer();
    b.append(StringUtil.padLeft(String.format("%.2f", this.getCompletion()), 6)).append("%");
    b.append(" (")
        .append(StringUtil.padLeft(this.current, Double.toString(this.total).length()))
        .append(" / ")
        .append(this.total)
        .append(")");
    return b.toString();
  }
}
