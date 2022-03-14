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
package org.eclipse.steady.goals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.enums.GoalType;

/**
 * <p>SequenceGoal class.</p>
 */
public class SequenceGoal extends AbstractAppGoal {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(SequenceGoal.class);

  private List<AbstractGoal> sequence = new ArrayList<AbstractGoal>();

  private double progress = 0;

  /**
   * <p>Constructor for SequenceGoal.</p>
   */
  public SequenceGoal() {
    super(GoalType.SEQUENCE);
  }

  /**
   * <p>addGoal.</p>
   *
   * @param _goal a {@link org.eclipse.steady.goals.AbstractGoal} object.
   */
  public void addGoal(AbstractGoal _goal) {
    this.sequence.add(_goal);
  }

  /**
   * {@inheritDoc}
   *
   * Checks whether one or more {@link AbstractGoal}s have been added.
   */
  @Override
  protected void prepareExecution() throws GoalConfigurationException {
    super.prepareExecution();

    // Add goals from configuration parameter if there are none yet
    if (this.sequence.isEmpty()) {

      final String goals[] =
          this.getConfiguration().getStringArray(CoreConfiguration.SEQ_DEFAULT, null);
      if (goals == null || goals.length == 0)
        throw new GoalConfigurationException("No goals have been added to the sequence");

      // Add one goal after the other
      for (String g : goals) {
        try {
          final GoalType gt = GoalType.parseGoal(g);
          this.addGoal(GoalFactory.create(gt, this.getGoalClient()));
        }
        // Thrown by parseGoal
        catch (IllegalArgumentException e) {
          throw new GoalConfigurationException(
              "Cannot add goal [" + g + "] to sequence: " + e.getMessage());
        }
        // Thrown by create
        catch (IllegalStateException e) {
          throw new GoalConfigurationException(
              "Cannot add goal [" + g + "] to sequence: " + e.getMessage());
        }
      }
    }

    // Loop over all goals and set configuration
    for (AbstractGoal g : this.sequence) {
      g.getGoalContext().setApplication(this.getApplicationContext());
      ((AbstractAppGoal) g).addAppPaths(new HashSet<Path>(this.getAppPaths()));
    }
  }

  /**
   * {@inheritDoc}
   *
   * Calls {@link AbstractGoal#executeSync()} for all goals that have been added to the sequence.
   */
  @Override
  protected void executeTasks() throws Exception {
    int i = 0;
    for (AbstractGoal g : this.sequence) {
      g.executeSync();
      this.progress = (double) ++i / (double) this.sequence.size();
    }
  }

  /**
   * Returns the progress, i.e., number of completed goals divided by total number of goals.
   *
   * @return a double.
   */
  public double getProgress() {
    return this.progress;
  }
}
