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
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.eclipse.steady.shared.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class DirUtilTest {

  @Test
  public void testFilterSubpaths() {
    final Path p1 = Paths.get("./src/test/resources/foo/bar/empty.txt");
    final Path p2 = Paths.get("./src/test/resources/foo/bar");
    final Path p3 = Paths.get("./src/test/resources/foo");
    final Path p4 = Paths.get("./src/test/resources/foo/empty");

    final Set<Path> filter = new HashSet<Path>();
    filter.add(p2);
    final Set<Path> to_be_filtered = new HashSet<Path>();
    to_be_filtered.add(p1);
    to_be_filtered.add(p3);
    to_be_filtered.add(p4);

    final Set<Path> r1 = DirUtil.filterSubpaths(to_be_filtered, filter, true);
    assertEquals(1, r1.size());

    final Set<Path> r2 = DirUtil.filterSubpaths(to_be_filtered, filter, false);
    assertEquals(2, r2.size());
  }

  @Test
  public void testGetAllFiles() {
    final File[] f1 = DirUtil.getAllFiles(new File("./src/test/resources/foo"), null);
    assertEquals(4, f1.length);

    final File[] f2 =
        DirUtil.getAllFiles(new File("./src/test/resources/foo"), new String[] {"bar"});
    assertEquals(3, f2.length);

    final File[] f3 =
        DirUtil.getAllFiles(new File("./src/test/resources/foo"), new String[] {"bar", "baz"});
    assertEquals(2, f3.length);
  }
}
