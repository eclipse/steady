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
import org.eclipse.steady.shared.enums.ConstructChangeType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.steady.ConstructChange;

public class TestPythonConstructs {
  private static final String REPO = "CVE-2016-2048";
  private static final String ZIP = "CVE-2016-2048.zip";
  private static String destPathToUnzip = System.getProperty("java.io.tmpdir");

  @Before
  public void setup() {
    String path = ConstructSet.class.getClassLoader().getResource(ZIP).getPath();
    if (!destPathToUnzip.endsWith(File.separator)) {
      destPathToUnzip = destPathToUnzip + File.separator;
    }

    ZipUtil.unzip(path, destPathToUnzip);
  }

  @Test
  public void testImport() {
    Commit commit = new Commit();
    commit.setBranch("master");
    commit.setCommitId("adbca5e4db42542575734b8e5d26961c8ada7265");
    commit.setDirectory(
        destPathToUnzip + REPO + File.separator + "adbca5e4db42542575734b8e5d26961c8ada7265");
    commit.setTimestamp("1447974481000");
    commit.setRepoUrl("https://github.com/django/django");

    Map<String, Set<ConstructChange>> changes = new HashMap<String, Set<ConstructChange>>();
    Set<ConstructChange> constructChanges = ConstructSet.identifyConstructChanges(commit, changes);
    assertEquals(6, constructChanges.size());
    ConstructChange constructChangeToValidate = null;
    for (ConstructChange constructChange : constructChanges) {
      if (constructChange
          .getConstruct()
          .getId()
          .getQualifiedName()
          .equals("django.contrib.admin.options")) {
        constructChangeToValidate = constructChange;
        break;
      }
    }
    assertEquals("1447974481000", constructChangeToValidate.getCommittedAt());
    assertEquals("https://github.com/django/django", constructChangeToValidate.getRepo());
    assertEquals("master:django/contrib/admin/options.py", constructChangeToValidate.getRepoPath());
    assertNotNull(constructChangeToValidate.getConstruct().getDigest());
    assertNotNull(constructChangeToValidate.getConstruct().getContent());
    assertEquals("options", constructChangeToValidate.getConstruct().getId().getName());
    assertEquals(
        "django.contrib.admin.options",
        constructChangeToValidate.getConstruct().getId().getQualifiedName());
    assertEquals(ConstructChangeType.MOD, constructChangeToValidate.getType());
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
