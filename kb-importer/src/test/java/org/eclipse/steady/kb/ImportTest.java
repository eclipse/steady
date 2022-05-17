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
package org.eclipse.steady.kb;

import org.eclipse.steady.kb.task.MockBackConnector;
import org.eclipse.steady.kb.model.Vulnerability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import java.util.HashMap;
import org.apache.commons.cli.Options;

import java.io.IOException;
import org.eclipse.steady.kb.exception.ValidationException;
import com.google.gson.JsonSyntaxException;
import org.eclipse.steady.backend.BackendConnectionException;
import org.junit.Test;

public class ImportTest {
  /*@Test
  public void getOptions() {
    Command command = new Import();
    Options options = command.getOptions();
    assertEquals(options.getOptions().size(), 4);
    assertTrue(options.hasOption("d"));
    assertTrue(options.hasOption("u"));
    assertTrue(options.hasOption("v"));
    assertTrue(options.hasOption("o"));
  }

  @Test
  public void validate() throws ValidationException {
    Command command = new Import();
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("d", ImportTest.class.getClassLoader().getResource("testRootDir1").getPath());
    command.validate(args);
  }

  @Test(expected = ValidationException.class)
  public void validationFail() throws ValidationException {
    Command command = new Import();
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("d", "invalidDir");
    command.validate(args);
  }*/

  @Test
  public void testImportSkipExistingBug()
      throws JsonSyntaxException, IOException, BackendConnectionException {
    Vulnerability vuln = new Vulnerability();
    vuln.setVulnId("CVE-TEST01");
    MockBackConnector mockBackendConnector = new MockBackConnector();
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("o", false);
    args.put("v", false);
    args.put("d", "");
    Manager manager = new Manager();
    Import command = new Import(manager, args, mockBackendConnector);
    command.run();
    assertNull(mockBackendConnector.getUploadJson());
  }
}
