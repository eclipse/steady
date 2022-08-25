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

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.task.MockBackConnector;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonSyntaxException;

public class TestImportCommand {

  /**
   * Enforce the use of {@link MockBackConnector}.
   */
  @Before
  public void setup() {
    try {
      Field instance_field = BackendConnector.class.getDeclaredField("instance");
      instance_field.setAccessible(true);
      instance_field.set(null, new MockBackConnector());
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testImportSkipExistingBug()
      throws JsonSyntaxException, IOException, BackendConnectionException {
    Vulnerability vuln = new Vulnerability();
    vuln.setVulnId("CVE-TEST01");
    MockBackConnector mockBackendConnector = new MockBackConnector();
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put(ImportCommand.OVERWRITE_OPTION, false);
    args.put(ImportCommand.VERBOSE_OPTION, false);
    args.put(ImportCommand.DIRECTORY_OPTION, "");
    Manager manager = new Manager();
    ImportCommand command = new ImportCommand(manager, args);
    command.run();
    assertNull(mockBackendConnector.getUploadJson());
  }
}
