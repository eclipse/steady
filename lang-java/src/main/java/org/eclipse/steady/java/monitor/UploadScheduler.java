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
package org.eclipse.steady.java.monitor;

import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.Logger;

/**
 * Calls a sequence methods of a given {@link ExecutionMonitor} in order upload collected
 * information to the Vulas backend. The {@link UploadScheduler} runs either once or periodically,
 * depending on the way it was constructed.
 */
public class UploadScheduler extends Observable implements Runnable, Observer {

  private static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(UploadScheduler.class);

  private long millis = -1;
  private int batchSize = -1;
  private boolean enabled = true;
  private ExecutionMonitor monitor = null;

  /**
   * <p>Constructor for UploadScheduler.</p>
   *
   * @param _monitor a {@link org.eclipse.steady.java.monitor.ExecutionMonitor} object.
   */
  public UploadScheduler(ExecutionMonitor _monitor) {
    this(_monitor, -1, -1);
  }
  /**
   * <p>Constructor for UploadScheduler.</p>
   *
   * @param _monitor a {@link org.eclipse.steady.java.monitor.ExecutionMonitor} object.
   * @param _millis a long.
   * @param _batch_size a int.
   */
  public UploadScheduler(ExecutionMonitor _monitor, long _millis, int _batch_size) {
    this.monitor = _monitor;
    this.millis = _millis;
    this.batchSize = _batch_size;
  }

  /**
   * <p>enabled.</p>
   */
  public void enabled() {
    this.enabled = true;
  }
  /**
   * <p>disable.</p>
   */
  public void disable() {
    this.enabled = false;
  }
  /**
   * <p>isEnabled.</p>
   *
   * @return a boolean.
   */
  public boolean isEnabled() {
    return this.enabled;
  }
  /**
   * <p>getInterval.</p>
   *
   * @return a long.
   */
  public long getInterval() {
    return this.millis;
  }
  /**
   * <p>Getter for the field <code>batchSize</code>.</p>
   *
   * @return a int.
   */
  public int getBatchSize() {
    return this.batchSize;
  }

  /**
   * Calls a sequence of methods of a {@link ExecutionMonitor}.
   */
  public void run() {
    // Final upload
    if (this.millis == -1) {

      // Stop further instrumentation and collection (that may happen in the course of the following
      // stmts)
      this.monitor.setPaused(true);
      DynamicTransformer.getInstance().setTransformationEnabled(false);

      // Upload stuff
      this.monitor.uploadInformation();
      this.monitor.awaitUpload();
      this.monitor.stopGoal();

      // Log instrumentation stats
      InstrumentationControl.logOverallStatistics();

      // Notify others that the final upload took place
      this.notifyObservers();
    }
    // Periodic uploads
    else {
      while (this.isEnabled()) {
        try {
          Thread.sleep(this.millis);
          if (this.isEnabled()) {
            this.monitor.uploadInformation(this.batchSize);

            // Log instrumentation stats
            InstrumentationControl.logOverallStatistics();
          }
        } catch (InterruptedException e) {
          UploadScheduler.log.error("Error in periodic trace upload: " + e.getMessage());
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * Disables periodic uploads (called by the shutdown uploader).
   */
  public void update(Observable obj, Object arg) {
    this.disable();
    UploadScheduler.log.info("Uploader disabled");
  }
}
