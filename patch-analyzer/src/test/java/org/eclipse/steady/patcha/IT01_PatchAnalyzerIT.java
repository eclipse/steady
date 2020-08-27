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
package org.eclipse.steady.patcha;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.eclipse.steady.ConstructChange;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.patcha.PatchAnalyzer;
import org.eclipse.steady.shared.categories.RequiresNetwork;
import org.eclipse.steady.shared.categories.Slow;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class IT01_PatchAnalyzerIT {

  // Disable upload for JUnit tests
  static {
    // VulasConfiguration.getGlobal().setProperty(CoreConfiguration.UPLOAD_ENABLED,
    // Boolean.valueOf(false));
    VulasConfiguration.getGlobal()
        .setProperty(CoreConfiguration.BACKEND_CONNECT, CoreConfiguration.ConnectType.OFFLINE);
  }

  /**
   * Analyzes the patch for CVE-2013-0239.
   */
  @Test
  @Category({Slow.class, RequiresNetwork.class})
  public void testSvnRepo() {
    try {
      final PatchAnalyzer pa =
          new PatchAnalyzer("http://svn.apache.org/repos/asf/cxf/", "CVE-2013-0239");
      final Map<String, String> revs = pa.searchCommitLog("CVE-2013-0239", null);

      // Only one commit with id 1438424
      assertTrue("Search must yield only 1 result", revs.size() == 1);
      final String rev = revs.keySet().iterator().next();
      assertEquals("1438424", rev);

      // Compare files and loop identified changes
      final Set<ConstructChange> changes = pa.identifyConstructChanges(rev);

      int i = 0;
      for (ConstructChange cc : changes) {
        System.out.println(++i + " change [" + cc + "]");
      }

      // Check that all changes have been found
      assertEquals("9 changes are done in this commit", 9, changes.size());

      // final String json = pa.toJSON(revs.keySet().toArray(new String[revs.keySet().size()]));
      // Check for json equality
    } catch (Exception e) {
      System.out.println(e.getMessage());
      assertTrue(false);
    }
  }
}
