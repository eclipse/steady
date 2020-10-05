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
package org.eclipse.steady.python;

import java.nio.file.Paths;

import org.junit.Test;

public class ProcessWrapperTest {

  @Test(expected = org.eclipse.steady.python.ProcessWrapperException.class)
  public void testIllegalChar() throws ProcessWrapperException {
    ProcessWrapper pw = new ProcessWrapper();
    pw.setCommand(Paths.get("pip"), "foo", "---..--", "\\asas");
  }

  @Test // (expected=com.sap.psr.vulas.python.ProcessWrapperException.class)
  public void testLegalChar() throws ProcessWrapperException {
    ProcessWrapper pw = new ProcessWrapper();
    pw.setCommand(Paths.get("pip"), "bar", "---..--");
  }
}
