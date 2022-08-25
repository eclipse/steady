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
package org.eclipse.steady.kb.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.eclipse.steady.kb.model.Commit;
import org.eclipse.steady.kb.model.Vulnerability;
import org.junit.Test;

import com.google.gson.JsonSyntaxException;

public class MetadataTest {
  @Test
  public void testGetVulnMetadata() throws JsonSyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir1").getPath();
    Vulnerability vuln = Metadata.getFromMetadata(path);
    assertEquals(3, vuln.getArtifacts().size());
    assertEquals(3, vuln.getNotes().size());
    assertEquals("COLLECTIONS-580", vuln.getVulnId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVulnRootDir() throws JsonSyntaxException, IOException {
    Metadata.getFromMetadata("rootDir1test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoVulnIdArg() throws Exception, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir2").getPath();
    Metadata.getFromMetadata(path);
  }

  @Test
  public void testMissingNonMandatoryParams() throws JsonSyntaxException, IOException {
    String path = "./src/test/resources/testRootDir3";
    Vulnerability vuln = Metadata.getFromMetadata(path);
    assertNull(vuln.getNotes());
    assertEquals("COLLECTIONS-580", vuln.getVulnId());
  }

  @Test
  public void testGetCommitMetadata() throws JsonSyntaxException, IOException {
    String path = "./src/test/resources/commitDir1";
    Commit commit = Metadata.getCommitMetadata(path);
    assertEquals("master", commit.getBranch());
    assertEquals("b2b8f4adc557e4ef1ee2fe5e0ab46866c06ec55b", commit.getCommitId());
    assertEquals("1447974481000", commit.getTimestamp());
    assertEquals("https://github.com/apache/commons-collections", commit.getRepoUrl());
    assertEquals(path, commit.getDirectory());
  }

  @Test
  public void testMetadataArtifacts() throws JsonSyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir4").getPath();
    Vulnerability vuln = Metadata.getFromMetadata(path);
    assertEquals(3, vuln.getArtifacts().size());
  }

  @Test
  public void testMetadataArtifactsPurl() throws JsonSyntaxException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String path = classLoader.getResource("testRootDir5").getPath();
    Vulnerability vuln = Metadata.getFromMetadata(path);
    assertEquals(1, vuln.getArtifacts().size());
    assertEquals(false, vuln.getArtifacts().get(0).getAffected());
    assertEquals(
        "pkg:maven://org.springframework.security/spring-security-config@5.1.1.RELEASE",
        vuln.getArtifacts().get(0).getId());
    assertEquals("Manual review(frontend)", vuln.getArtifacts().get(0).getReason());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCommitMetadataDir() throws JsonSyntaxException, IOException {
    Metadata.getFromMetadata("commitDir2");
  }

  @Test
  public void testGetFromYaml() throws IOException {
    Vulnerability vuln = Metadata.getFromYaml("./src/test/resources/testRootDir1/statement.yaml");
    assertEquals(3, vuln.getArtifacts().size());
    assertEquals(3, vuln.getNotes().size());
    assertEquals("COLLECTIONS-580", vuln.getVulnId());
  }
}
