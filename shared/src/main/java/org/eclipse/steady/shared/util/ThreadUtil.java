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
package org.eclipse.steady.shared.util;

import org.apache.logging.log4j.Logger;

/**
 * <p>ThreadUtil class.</p>
 *
 */
public class ThreadUtil {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /** Constant <code>NO_OF_THREADS="vulas.core.noThreads"</code> */
  public static final String NO_OF_THREADS = "vulas.core.noThreads";

  /**
   * Returns true if the value of configuration setting {@link NO_OF_THREADS} is equal to AUTO, false otherwise.
   * @return
   */
  private static boolean isAutoThreading(VulasConfiguration _cfg) {
    return "AUTO".equalsIgnoreCase(_cfg.getConfiguration().getString(NO_OF_THREADS, null));
  }

  /**
   * Returns the number of threads to be used for parallelized processing steps, thereby taking the configuration setting
   * {@link NO_OF_THREADS} and the number of cores into account.
   *
   * @param _multiply_if_auto a int.
   * @return a int.
   */
  public static final int getNoThreads(final int _multiply_if_auto) {
    return getNoThreads(VulasConfiguration.getGlobal(), _multiply_if_auto);
  }

  /**
   * Returns the number of threads to be used for parallelized processing steps, thereby taking the configuration setting
   * {@link NO_OF_THREADS} and the number of cores into account.
   *
   * @param _cfg a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   * @param _multiply_if_auto a int.
   * @return a int.
   */
  public static final int getNoThreads(final VulasConfiguration _cfg, final int _multiply_if_auto) {
    int number = 1;
    if (isAutoThreading(_cfg)) {
      number = Runtime.getRuntime().availableProcessors() * _multiply_if_auto;
      log.info(
          "Auto-threading enabled: Number of threads is ["
              + _multiply_if_auto
              + " x "
              + Runtime.getRuntime().availableProcessors()
              + " cores]");
    } else {
      final String value = _cfg.getConfiguration().getString(NO_OF_THREADS, "1");
      try {
        number = Integer.parseInt(value);
        log.info("Auto-threading disabled: Number of threads is [" + number + "]");
      } catch (NumberFormatException nfe) {
        number = 1;
        log.error(
            "Auto-threading disabled: Configuration setting ["
                + NO_OF_THREADS
                + "] must be AUTO or integer, but is ["
                + value
                + "], defaulting to [1]");
      }
    }
    return number;
  }

  /**
   * <p>getNoThreads.</p>
   *
   * @return a int.
   */
  public static int getNoThreads() {
    return getNoThreads(1);
  }
}
