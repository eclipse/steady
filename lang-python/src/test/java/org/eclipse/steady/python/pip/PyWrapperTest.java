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
package org.eclipse.steady.python.pip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PyWrapperTest {

  @Test
  public void testPythonVersion() {
    final List<String> list = new ArrayList<String>();
    list.add("python");
    list.add("--version");

    try {
      // Perform call
      final ProcessBuilder pb = new ProcessBuilder(list);

      // Start and wait
      final Process process = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      System.out.print("Output of [python --version]:");
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Test
  public void testPipVersion() {
    final List<String> list = new ArrayList<String>();
    list.add("python");
    list.add("-m");
    list.add("pip");
    list.add("--version");

    try {
      // Perform call
      final ProcessBuilder pb = new ProcessBuilder(list);

      // Start and wait
      final Process process = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      System.out.print("Output of [python -m pip --version]:");
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
