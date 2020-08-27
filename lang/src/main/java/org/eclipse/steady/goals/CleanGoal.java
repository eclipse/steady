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
package org.eclipse.steady.goals;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.enums.GoalType;
import org.eclipse.steady.shared.json.model.Application;

/**
 * <p>CleanGoal class.</p>
 *
 */
public class CleanGoal extends AbstractAppGoal {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * <p>Constructor for CleanGoal.</p>
   */
  public CleanGoal() {
    super(GoalType.CLEAN);
  }

  /** {@inheritDoc} */
  @Override
  /*
   * This method cleans the backend and also eventually purges the older versions of the application.
   * If application version (GAV) exists, it eventually purges the keepLast versions, otherwise it eventually purges the (keepLast-1) versions
   * @see com.sap.psr.vulas.goals.AbstractGoal#executeTasks()
   */
  protected void executeTasks() throws Exception {
    final BackendConnector bc = BackendConnector.getInstance();
    final Application app = this.getApplicationContext();

    int keepLast =
        this.getConfiguration()
            .getConfiguration()
            .getInt(CoreConfiguration.CLEAN_PURGE_KEEP_LAST, 3);

    // Clean
    if (bc.isAppExisting(this.getGoalContext(), app)) {
      bc.cleanApp(
          this.getGoalContext(),
          app,
          this.getConfiguration()
              .getConfiguration()
              .getBoolean(CoreConfiguration.CLEAN_HISTORY, false));
    } else {
      log.info("App [" + app + "] does not exist in backend, thus, cleaning not possible");
      this.skipGoalUpload();
      // in case the GAV does not exist, then reducing by 1 the number of versions to be kept
      if (keepLast > 0) {
        --keepLast;
      }
    }

    // Purge versions
    if (this.getConfiguration()
        .getConfiguration()
        .getBoolean(CoreConfiguration.CLEAN_PURGE_VERSIONS, false)) {
      bc.purgeAppVersions(this.getGoalContext(), app, keepLast);
    }
  }
}
