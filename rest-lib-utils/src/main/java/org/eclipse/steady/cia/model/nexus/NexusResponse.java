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
package org.eclipse.steady.cia.model.nexus;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>NexusResponse class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusResponse {

  private Collection<NexusSearch> searches;

  /**
   * <p>Constructor for NexusResponse.</p>
   */
  public NexusResponse() {}

  /**
   * <p>Getter for the field <code>searches</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<NexusSearch> getSearches() {
    return searches;
  }

  /**
   * <p>Setter for the field <code>searches</code>.</p>
   *
   * @param searches a {@link java.util.Collection} object.
   */
  public void setSearches(Collection<NexusSearch> searches) {
    this.searches = searches;
  }
}