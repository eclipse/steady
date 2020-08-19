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
package com.sap.psr.vulas.shared.json.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>Ratio class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ratio extends AbstractMetric {

  private double count;

  private double total;

  /**
   * <p>Constructor for Ratio.</p>
   */
  public Ratio() {
    this(null, 0, 0);
  }

  /**
   * <p>Constructor for Ratio.</p>
   *
   * @param _name a {@link java.lang.String} object.
   */
  public Ratio(String _name) {
    this(_name, 0, 0);
  }

  /**
   * <p>Constructor for Ratio.</p>
   *
   * @param _name a {@link java.lang.String} object.
   * @param _count a double.
   * @param _total a double.
   * @throws java.lang.IllegalArgumentException if count GT total
   */
  public Ratio(String _name, double _count, double _total) throws IllegalArgumentException {
    super(_name);
    if (_count > _total)
      throw new IllegalArgumentException("Count [" + _count + "] GT total [" + _total + "]");
    this.count = _count;
    this.total = _total;
  }

  /**
   * <p>Getter for the field <code>count</code>.</p>
   *
   * @return a double.
   */
  public double getCount() {
    return count;
  }

  /**
   * <p>incrementCount.</p>
   */
  public void incrementCount() {
    this.count = this.count + 1d;
  }

  /**
   * <p>Setter for the field <code>count</code>.</p>
   *
   * @param count a double.
   */
  public void setCount(double count) {
    this.count = count;
  }

  /**
   * <p>Getter for the field <code>total</code>.</p>
   *
   * @return a double.
   */
  public double getTotal() {
    return total;
  }

  /**
   * <p>incrementTotal.</p>
   *
   * @param _inc a double.
   */
  public void incrementTotal(double _inc) {
    this.total = this.total + _inc;
  }

  /**
   * <p>incrementTotal.</p>
   */
  public void incrementTotal() {
    this.total = this.total + 1d;
  }

  /**
   * <p>Setter for the field <code>total</code>.</p>
   *
   * @param total a double.
   */
  public void setTotal(double total) {
    this.total = total;
  }

  /**
   * Returns the ratio as percentage, or -1 if total EQ 0.
   *
   * @return a double.
   */
  public double getRatio() {
    return (this.getTotal() == 0d ? -1d : this.getCount() / this.getTotal());
  }
}
