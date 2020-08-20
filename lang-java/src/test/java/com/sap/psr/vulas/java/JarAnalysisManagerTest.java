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
package com.sap.psr.vulas.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.sap.psr.vulas.shared.categories.Slow;
import com.sap.psr.vulas.shared.util.FileSearch;

public class JarAnalysisManagerTest {

  @Test
  @Category(Slow.class)
  public void testStartAnalysis() {
    try {
      final ArchiveAnalysisManager jam = new ArchiveAnalysisManager(4, -1, false, null);
      final FileSearch fs = new FileSearch(new String[] {"war"});
      jam.startAnalysis(fs.search(Paths.get("./src/test/resources")), null);
      final Set<JarAnalyzer> analyzers = jam.getAnalyzers();
      assertEquals(16, analyzers.size()); // 1 for the WAR and 15 for JARs in WEB-INF/lib
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
}
