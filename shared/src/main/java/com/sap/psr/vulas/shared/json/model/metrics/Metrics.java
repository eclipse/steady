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

import java.util.Collection;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A collection of metrics of type {@link Percentage}, {@link Counter} or {@link Ratio}.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metrics {

  private Collection<Ratio> ratios = null;

  private Collection<Percentage> percentages = null;

  private Collection<Counter> counters = null;

  /**
   * <p>addRatio.</p>
   *
   * @param _r a {@link com.sap.psr.vulas.shared.json.model.metrics.Ratio} object.
   */
  public void addRatio(Ratio _r) {
    if (this.getRatios() == null) {
      this.setRatios(new TreeSet<Ratio>());
    }
    this.getRatios().add(_r);
  }

  /**
   * <p>Getter for the field <code>ratios</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Ratio> getRatios() {
    return ratios;
  }
  /**
   * <p>Setter for the field <code>ratios</code>.</p>
   *
   * @param ratios a {@link java.util.Collection} object.
   */
  public void setRatios(Collection<Ratio> ratios) {
    this.ratios = ratios;
  }

  /**
   * <p>addPercentage.</p>
   *
   * @param _r a {@link com.sap.psr.vulas.shared.json.model.metrics.Percentage} object.
   */
  public void addPercentage(Percentage _r) {
    if (this.getPercentages() == null) {
      this.setPercentages(new TreeSet<Percentage>());
    }
    this.getPercentages().add(_r);
  }

  /**
   * <p>Getter for the field <code>percentages</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Percentage> getPercentages() {
    return this.percentages;
  }
  /**
   * <p>Setter for the field <code>percentages</code>.</p>
   *
   * @param _percentages a {@link java.util.Collection} object.
   */
  public void setPercentages(Collection<Percentage> _percentages) {
    this.percentages = _percentages;
  }

  /**
   * <p>addCounter.</p>
   *
   * @param _c a {@link com.sap.psr.vulas.shared.json.model.metrics.Counter} object.
   */
  public void addCounter(Counter _c) {
    if (this.getCounters() == null) {
      this.setCounters(new TreeSet<Counter>());
    }
    this.getCounters().add(_c);
  }

  /**
   * <p>Getter for the field <code>counters</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Counter> getCounters() {
    return this.counters;
  }
  /**
   * <p>Setter for the field <code>counters</code>.</p>
   *
   * @param _counters a {@link java.util.Collection} object.
   */
  public void setCounters(Collection<Counter> _counters) {
    this.counters = _counters;
  }
}
