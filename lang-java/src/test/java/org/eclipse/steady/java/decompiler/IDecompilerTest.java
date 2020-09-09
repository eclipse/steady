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
package org.eclipse.steady.java.decompiler;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.eclipse.steady.Construct;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.FileAnalyzerFactory;
import org.eclipse.steady.java.JavaClassId;
import org.eclipse.steady.java.JavaId;
import org.junit.Test;

public class IDecompilerTest {

  /** Test class to be decompiled. */
  class NonStaticInner {
    NonStaticInner() {}

    void foo() {}
  }

  /** Test class to be decompiled. */
  static class StaticInner {
    StaticInner() {}

    void foo() {}
  }

  /**
   * Test whether the decompiler properly constructs the names of inner classes (in the form Outer$Inner).
   * As explained in the Procyon ticket #283 (https://bitbucket.org/mstrobel/procyon/issues/283), this
   * does not work for an inner class from Apache FileUpload.
   */
  @Test
  public void testDecompileAnonClass() {
    try {
      // Decompile and get constructs
      final IDecompiler decompiler = new ProcyonDecompiler();
      final File java_source_file =
          decompiler.decompileClassFile(
              new File(
                  "./target/test-classes/org/eclipse/steady/java/decompiler/IDecompilerTest$NonStaticInner.class"));
      final FileAnalyzer jfa = FileAnalyzerFactory.buildFileAnalyzer(java_source_file);
      final Map<ConstructId, Construct> constructs = jfa.getConstructs();

      // Expected construct
      JavaClassId inner_class =
          JavaId.parseClassQName(
              "org.eclipse.steady.java.decompiler.IDecompilerTest$NonStaticInner");

      // TODO: Change as soon (if ever) this is fixed
      assertTrue(!constructs.containsKey(inner_class));
    } catch (FileAnalysisException e) {
    }
  }
}
