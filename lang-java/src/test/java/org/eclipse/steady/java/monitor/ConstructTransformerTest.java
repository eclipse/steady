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
package org.eclipse.steady.java.monitor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.steady.java.monitor.Loader;
import org.eclipse.steady.java.monitor.LoaderHierarchy;
import org.eclipse.steady.java.monitor.trace.PathNode;
import org.eclipse.steady.java.monitor.trace.StackTraceUtil;
import org.junit.Test;

public class ConstructTransformerTest {

  @Test
  public void testLoaderHierarchy() {
    final LoaderHierarchy h = new LoaderHierarchy();
    final Loader l = h.add(this.getClass().getClassLoader());
    assertEquals(l.isLeaf(), true);
    assertEquals(l.isRoot(), false);
  }

  /**
   * This method overloads the other method having the same name, which allows testing whether StackTraceUtil can successfully identify the right method based on line numbers.
   * @param _i
   */
  public Throwable stacktraceTest(int _i) {
    return new Throwable();
  }

  @Test
  public void stacktraceTest() {
    StackTraceUtil util = new StackTraceUtil();

    util.setStopAtJUnit(true);
    List<PathNode> path = util.transformStackTrace(this.stacktraceTest(1).getStackTrace(), null);
    assertEquals(true, path.size() == 2);

    util.setStopAtJUnit(false);
    path = util.transformStackTrace(this.stacktraceTest(1).getStackTrace(), null);
    assertEquals(true, path.size() > 1);
  }
}
