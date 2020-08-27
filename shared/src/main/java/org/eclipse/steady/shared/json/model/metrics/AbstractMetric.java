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
package org.eclipse.steady.shared.json.model.metrics;

/**
 * <p>Abstract AbstractMetric class.</p>
 *
 */
public abstract class AbstractMetric implements Comparable {

  private String name;

  /**
   * <p>Constructor for AbstractMetric.</p>
   *
   * @param _name a {@link java.lang.String} object.
   */
  protected AbstractMetric(String _name) {
    this.name = _name;
  }

  /**
   * <p>Getter for the field <code>name</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getName() {
    return name;
  }

  /**
   * <p>Setter for the field <code>name</code>.</p>
   *
   * @param name a {@link java.lang.String} object.
   */
  public void setName(String name) {
    this.name = name;
  }

  /** {@inheritDoc} */
  @Override
  public int compareTo(Object o) {
    if (o instanceof AbstractMetric)
      return this.getName().compareToIgnoreCase(((AbstractMetric) o).getName());
    else
      throw new IllegalArgumentException(
          "Expected object of type ["
              + AbstractMetric.class.getSimpleName()
              + "], got ["
              + o.getClass().getSimpleName()
              + "]");
  }
}
