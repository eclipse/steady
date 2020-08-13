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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.junit.Test;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;

public class AarAnalyzerTest {

  @Test
  public void testAarAnalyzer() {
    try {
      final AarAnalyzer aa = new AarAnalyzer();
      aa.analyze(new File("./src/test/resources/cucumber-android-4.3.0.aar"));
      final Set<ConstructId> cids = aa.getConstructIds();
      assertTrue(!cids.isEmpty());
    } catch (FileAnalysisException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
}
