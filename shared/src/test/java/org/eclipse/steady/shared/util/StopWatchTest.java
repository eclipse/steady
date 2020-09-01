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
package org.eclipse.steady.shared.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StopWatchTest {

  @Test
  public void testGetRuntimeMillis() {
    final StopWatch sw = new StopWatch("foo").start();
    sw.lap("bar", false); // Should not be printed due to threshold
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
    sw.lap("baz", true);
    sw.stop();
    final long rt = sw.getRuntimeMillis();
    assertTrue(rt < 50000);
  }

  @Test
  public void testProgressTracker() {
    int total = 67;
    final StopWatch sw = new StopWatch("foo").setTotal(total).start();
    for (int i = 0; i < total; i++) {
      try {
        Thread.sleep(Math.round(Math.random() * 178));
        sw.progress(); // log messages every 5%
      } catch (InterruptedException e) {
      }
    }
    sw.stop();
    System.out.println(
        "Avg lap time: "
            + StringUtil.nanoToFlexDurationString(sw.getAvgLapTime())
            + ", max lap time: "
            + StringUtil.nanoToFlexDurationString(sw.getMaxLapTime()));
  }
}
