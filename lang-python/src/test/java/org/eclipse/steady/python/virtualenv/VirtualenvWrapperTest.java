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
package org.eclipse.steady.python.virtualenv;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.python.ProcessWrapperException;
import org.eclipse.steady.python.pip.PipInstalledPackage;
import org.eclipse.steady.shared.categories.Slow;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.StringList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class VirtualenvWrapperTest {

  /**
   * Attention: Runs long...
   *
   * @throws IllegalArgumentException
   * @throws ProcessWrapperException
   * @throws FileAnalysisException
   */
  @Test
  @Category(Slow.class)
  public void testCreateVirtualenv()
      throws IllegalArgumentException, ProcessWrapperException, FileAnalysisException {

    // Create virtualenv
    final Path project = Paths.get("src", "test", "resources", "cf-helloworld");
    final VirtualenvWrapper vew = new VirtualenvWrapper(project);
    final Path ve_path = vew.getPathToVirtualenv();
    assertTrue(FileUtil.isAccessibleDirectory(ve_path));

    // Get packages
    final Set<PipInstalledPackage> packs = vew.getInstalledPackages();
    assertTrue(packs.size() >= 8);

    // Get rid of the project itself
    final Set<PipInstalledPackage> filtered_packs =
        PipInstalledPackage.filterUsingArtifact(
            packs, new StringList().add("cf-helloworld"), false);
    assertTrue(filtered_packs.size() >= 7);

    // Get SHA1 for every package
    for (PipInstalledPackage p : filtered_packs) {
      final String sha1 = p.getDigest();
      assertTrue(sha1 != null && !sha1.equals(""));
    }

    // Get constructs for every package
    for (PipInstalledPackage p : filtered_packs) {
      final Collection<ConstructId> constructs = p.getLibrary().getConstructs();
      assertTrue(constructs != null && constructs.size() > 0);
    }
  }
}
