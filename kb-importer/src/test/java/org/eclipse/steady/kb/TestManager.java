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

import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import com.google.gson.JsonSyntaxException;
import org.eclipse.steady.backend.BackendConnectionException;
import org.junit.Test;

public class TestManager {

  @Test
  public void testStartList() throws JsonSyntaxException, IOException, BackendConnectionException {
    String statementsPath = Manager.class.getClassLoader().getResource("statements").getPath();
    MockBackConnector mockBackendConnector = new MockBackConnector();
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put(ImportCommand.OVERWRITE_OPTION, true);
    args.put(ImportCommand.VERBOSE_OPTION, false);
    args.put(ImportCommand.SKIP_CLONE_OPTION, false);
    Manager manager = new Manager(mockBackendConnector);

    List<String> vulnIds = manager.identifyVulnerabilitiesToImport(statementsPath);
    manager.startList(statementsPath, args, vulnIds);

    Manager.VulnStatus vulnStatus1 = manager.getVulnStatus("CVE-2018-1270");

    org.junit.Assert.assertEquals(vulnStatus1, Manager.VulnStatus.IMPORTED);
    org.junit.Assert.assertEquals(mockBackendConnector.getUploadedChangeLists().size(), 1);
    org.junit.Assert.assertEquals(mockBackendConnector.getUploadedLibraries().size(), 0);
  }
}
