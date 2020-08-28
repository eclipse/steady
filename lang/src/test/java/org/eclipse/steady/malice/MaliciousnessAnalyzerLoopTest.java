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
package org.eclipse.steady.malice;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Set;

import org.junit.Test;

public class MaliciousnessAnalyzerLoopTest {

  @Test
  public void testLoop() {
    final MaliciousnessAnalyzerLoop loop = new MaliciousnessAnalyzerLoop();
    final Set<MaliciousnessAnalysisResult> results =
        loop.isMalicious(new File("src/test/resources/zip-slip.zip"));
    assertTrue(results.size() > 0);
    assertEquals(1d, results.iterator().next().getResult(), 0d);
  }
}
