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
package org.eclipse.steady.backend.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>PackageStatistics class.</p>
 *
 */
public class PackageStatistics {

  /**
   * Package to statistics
   */
  private Map<ConstructId, ConstructIdFilter> constructIds =
      new HashMap<ConstructId, ConstructIdFilter>();

  /**
   * <p>Constructor for PackageStatistics.</p>
   *
   * @param _constructs_ids a {@link java.util.Collection} object.
   */
  public PackageStatistics(Collection<ConstructId> _constructs_ids) {
    ConstructIdFilter stats = null;
    ConstructId pid = null;
    if (_constructs_ids != null) {
      for (ConstructId cid : _constructs_ids) {
        pid = ConstructId.getPackageOf(cid);
        stats = this.constructIds.get(pid);
        if (stats == null) {
          stats = new ConstructIdFilter(null);
          this.constructIds.put(pid, stats);
        }
        stats.addConstructId(cid);
      }
    }
  }

  /**
   * <p>countConstructTypesPerPackage.</p>
   *
   * @return a {@link java.util.Map} object.
   */
  @JsonProperty(value = "packageCounters")
  public Map<ConstructId, ConstructIdFilter> countConstructTypesPerPackage() {
    return this.constructIds;
  }
}
