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
package org.eclipse.steady.cg;

import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.enums.PathSource;
import org.eclipse.steady.shared.util.ConstructIdUtil;

/**
 * <p>A2CGoal class.</p>
 *
 */
public class A2CGoal extends AbstractReachGoal {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private Set<org.eclipse.steady.shared.json.model.ConstructId> entryPoints = null;

  /**
   * <p>Constructor for A2CGoal.</p>
   */
  public A2CGoal() {
    super(GoalType.A2C);
  }

  /**
   * <p>Getter for the field <code>entryPoints</code>.</p>
   *
   * @return a {@link java.util.Set} object.
   */
  protected final Set<org.eclipse.steady.shared.json.model.ConstructId> getEntryPoints() {
    if (this.entryPoints == null) {
      // Filter app constructs (if requested)
      final String[] filter =
          this.getConfiguration()
              .getConfiguration()
              .getStringArray(ReachabilityConfiguration.REACH_CONSTR_FILTER);
      if (filter != null && filter.length > 0 && !(filter.length == 1 && filter[0].equals(""))) {
        this.entryPoints = ConstructIdUtil.filterWithRegex(this.getAppConstructs(), filter);
      } else {
        this.entryPoints = this.getAppConstructs();
      }
    }
    return this.entryPoints;
  }

  /**
   * {@inheritDoc}
   *
   * Sets the application constructs as entry points of the {@link ReachabilityAnalyzer}.
   */
  protected final void setEntryPoints(ReachabilityAnalyzer _ra) {
    _ra.setEntryPoints(
        this.getEntryPoints(),
        PathSource.A2C,
        this.getConfiguration()
            .getConfiguration()
            .getBoolean(ReachabilityConfiguration.REACH_EXIT_UNKOWN_EP, false));
  }
}
