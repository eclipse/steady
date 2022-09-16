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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.eclipse.steady.kb.model.Commit;
import org.eclipse.steady.kb.util.ConstructSet;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.steady.ConstructChange;

public class TestConstructs {
  private static final String REPO = "rootDir";
  private static final String ZIP = "COLLECTIONS-580.zip";
  private static String destPathToUnzip = System.getProperty("java.io.tmpdir");

  @Before
  public void setup() {
    String path = ConstructSet.class.getClassLoader().getResource(ZIP).getPath();
    // For some OS slash is not added at the end of tmp dir
    if (!destPathToUnzip.endsWith(File.separator)) {
      destPathToUnzip = destPathToUnzip + File.separator;
    }

    ZipUtil.unzip(path, destPathToUnzip);
  }

  @Test
  public void testImportCommand() {
    Commit commit = new Commit();
    commit.setBranch("master");
    commit.setCommitId("b2b8f4adc557e4ef1ee2fe5e0ab46866c06ec55b");
    commit.setDirectory(
        destPathToUnzip + REPO + File.separator + "b2b8f4adc557e4ef1ee2fe5e0ab46866c06ec55b");
    commit.setTimestamp("1447974481000");
    commit.setRepoUrl("https://github.com/apache/commons-collections");

    Map<String, Set<ConstructChange>> changes = new HashMap<String, Set<ConstructChange>>();
    Set<ConstructChange> constructChanges = ConstructSet.identifyConstructChanges(commit, changes);
    ConstructChange contructChangeFirstElement = (ConstructChange) constructChanges.toArray()[0];
    assertEquals(150, constructChanges.size());
    assertEquals("1447974481000", contructChangeFirstElement.getCommittedAt());
    assertEquals(
        "https://github.com/apache/commons-collections", contructChangeFirstElement.getRepo());
    assertNotNull(contructChangeFirstElement.getRepoPath());
    assertNotNull(contructChangeFirstElement.getConstruct());
    assertNotNull(contructChangeFirstElement.getType());
  }

  @AfterClass
  public static void cleanup() {
    try {
      FileUtils.deleteDirectory(new File(destPathToUnzip + REPO));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
