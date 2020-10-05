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
package org.eclipse.steady.shared.json.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Percentage metrics. Compared to {@link Ratio}, it does not have a counter and total from which
 * the percentage is computed.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Percentage extends AbstractMetric {

  private double percentage;

  /**
   * <p>Constructor for Percentage.</p>
   */
  public Percentage() {
    this(null, 0);
  }

  /**
   * <p>Constructor for Percentage.</p>
   *
   * @param _name a {@link java.lang.String} object.
   * @param _percentage a double.
   */
  public Percentage(String _name, double _percentage) {
    super(_name);
    this.setPercentage(_percentage);
  }

  /**
   * Sets the metric.
   *
   * @param percentage a double.
   * @throws java.lang.IllegalArgumentException if the value is LT 0 or GT 1
   */
  public void setPercentage(double percentage) throws IllegalArgumentException {
    if (percentage < 0 || percentage > 1)
      throw new IllegalArgumentException("Expected percentage, got [" + percentage + "]");
    this.percentage = percentage;
  }

  /**
   * <p>Getter for the field <code>percentage</code>.</p>
   *
   * @return a double.
   */
  public double getPercentage() {
    return this.percentage;
  }
}
