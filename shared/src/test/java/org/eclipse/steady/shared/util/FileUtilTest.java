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
package org.eclipse.steady.shared.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.junit.Test;

public class FileUtilTest {

  @Test
  public void testGetDigest() {
    final File f = new File("./src/test/resources/foo.txt");

    final String sha1 = FileUtil.getSHA1(f).toLowerCase();
    final String expected_sha1 = "c7567e8b39e2428e38bf9c9226ac68de4c67dc39";
    assertEquals(expected_sha1, sha1);

    final String md5 = FileUtil.getDigest(f, DigestAlgorithm.MD5).toLowerCase();
    final String expected_md5 = "ab07acbb1e496801937adfa772424bf7";
    assertEquals(expected_md5, md5);
  }

  @Test
  public void testGetCharset() {
    final VulasConfiguration cfg = new VulasConfiguration();
    cfg.setProperty(VulasConfiguration.CHARSET, "foo");
    final Charset cs = FileUtil.getCharset();
    assertEquals("UTF-8", cs.name());
  }

  @Test
  public void testGetCRC32File() {
    assertEquals(
        2321822010l, FileUtil.getCRC32(new File("./src/test/resources/steady-test.properties")));
  }

  @Test
  public void testGetCRC32Bytes() {
    assertEquals(4157704578l, FileUtil.getCRC32("Hello".getBytes()));
  }

  @Test
  public void testCopyFile() {
    try {
      final VulasConfiguration cfg = new VulasConfiguration();
      final Path tmp_dir = cfg.getTmpDir();
      final Path source_file = Paths.get("./src/test/resources/Outer.jar");
      final Path target_file = FileUtil.copyFile(source_file, tmp_dir);
      assertEquals(
          FileUtil.getDigest(source_file.toFile(), DigestAlgorithm.SHA1),
          FileUtil.getDigest(target_file.toFile(), DigestAlgorithm.SHA1));
    } catch (IOException e) {
      e.printStackTrace();
      assertEquals(true, false);
    }
  }

  @Test
  public void testGetJarFilePathsForResources() {
    final String[] resources = new String[] {"LICENSE-junit.txt"}; // Contained in junit-4.12.jar
    final Set<String> jars =
        FileUtil.getJarFilePathsForResources(FileUtil.class.getClassLoader(), resources);
    assertTrue(jars.size() == 1);
  }

  @Test
  public void testGetJarFilePaths() {
    // As of Java 9, the system class loader is no longer a URLClassLoader
    // https://stackoverflow.com/questions/46694600/java-9-compatability-issue-with-classloader-getsystemclassloader
    // https://blog.codefx.org/java/java-9-migration-guide/#Casting-To-URL-Class-Loader
    final ClassLoader cl = FileUtil.class.getClassLoader();
    if (cl instanceof URLClassLoader) {
      final Set<String> jars = FileUtil.getJarFilePaths((URLClassLoader) cl);
      assertTrue(!jars.isEmpty());
    }
  }

  @Test
  public void testGetFileName() {
    final String name1 = FileUtil.getFileName("hello.min.js", false);
    assertEquals("hello.min", name1);

    final String name2 = FileUtil.getFileName("hello.js", false);
    assertEquals("hello", name2);

    final String name3 =
        FileUtil.getFileName(Paths.get("./project/js/hello.io.min.js").toString(), false);
    assertEquals("hello.io.min", name3);
  }
}
