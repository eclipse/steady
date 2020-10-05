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
package org.eclipse.steady.cli;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ServiceLoader;

import org.eclipse.steady.DirAnalyzer;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.FileAnalyzerFactory;
import org.eclipse.steady.java.JavaFileAnalyzer2;
import org.eclipse.steady.python.PythonFileAnalyzer;
import org.junit.Test;

public class FileAnalyzerTest {

  /**
   * Checks whether the well-known FileAnaylyzers can be found.
   */
  @Test
  public void testPresenceOfWellknownFileAnalyzers() {
    int i = 0;
    final ServiceLoader<FileAnalyzer> loader = ServiceLoader.load(FileAnalyzer.class);
    for (FileAnalyzer la : loader) {
      i++;
      System.out.println("File analyzer #" + i + ": " + la.getClass().getName());
    }
    assertTrue(i >= 7);
  }

  /**
   * Checks whether some of the well-known FileAnaylyzer are returned by the FileAnalyzerFactory.
   */
  @Test
  public void testWellknownFileAnalyzers() {
    final File py = new File("./src/test/resources/file.py");
    FileAnalyzer fa = FileAnalyzerFactory.buildFileAnalyzer(py);
    assertTrue(fa instanceof PythonFileAnalyzer);

    final File d = new File("./src/test/resources");
    fa = FileAnalyzerFactory.buildFileAnalyzer(d);
    assertTrue(fa instanceof DirAnalyzer);

    final File ja = new File("./src/test/resources/file.java");
    fa = FileAnalyzerFactory.buildFileAnalyzer(ja);
    assertTrue(fa instanceof JavaFileAnalyzer2);
  }
}
