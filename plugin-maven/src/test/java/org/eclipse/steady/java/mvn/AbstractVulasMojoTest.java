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
package org.eclipse.steady.java.mvn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.steady.shared.json.model.LibraryId;
import org.junit.Test;

public class AbstractVulasMojoTest {

  @Test
  public void testParseGAPV() {
    final MvnPluginReport m = new MvnPluginReport();
    assertEquals(
        new LibraryId("commons-fileupload", "commons-fileupload", "1.2.1"),
        m.parseGAPV("commons-fileupload:commons-fileupload:jar:1.2.1"));
    assertTrue(m.parseGAPV("commons-fileupload:commons-fileupload:1.2.1") == null);
  }

  @Test
  public void testGetParent() {
    // Construct the dependency trail as provided by Artifact.getDependencyTrail()
    final List<String> trail = new ArrayList<String>();
    trail.add("commons-fileupload:commons-fileupload:jar:1.2.1");
    trail.add("commons-codec:commons-codec:jar:1.2.1");
    trail.add("commons-lang:commons-lang:jar:1.2.1");

    final MvnPluginReport m = new MvnPluginReport();
    final LibraryId parent = m.getParent(trail);
    assertEquals(new LibraryId("commons-codec", "commons-codec", "1.2.1"), parent);
  }
}
