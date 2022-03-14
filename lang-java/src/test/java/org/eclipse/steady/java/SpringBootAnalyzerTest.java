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
package org.eclipse.steady.java;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.Set;

import org.eclipse.steady.ConstructId;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.shared.categories.Slow;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class SpringBootAnalyzerTest {

  @Before
  public void removeInstrumentedArchives() {
    for (String n : new String[] {"boot-app-steady-instr.jar"}) {
      final File f = new File("./target/" + n);
      if (f.exists()) {
        f.delete();
      }
    }
  }

  @Test
  @Category(Slow.class)
  public void testAnalyze() {
    try {
      final SpringBootAnalyzer wa = new SpringBootAnalyzer();
      wa.analyze(new File("./src/test/resources/boot-app.jar"));
      wa.setWorkDir(Paths.get("./target"));
      wa.setRename(true);
      SpringBootAnalyzer.setAppContext(
          new Application("dummy-group", "dummy-artifact", "0.0.1-SNAPSHOT"));
      wa.call();

      // 15 archives in WEB-INF/lib
      final Set<FileAnalyzer> fas = wa.getChilds(true);
      // assertEquals(15, fas.size());

      // 15 constructs in classes in WEB-INF/classes
      final Set<ConstructId> cids = wa.getConstructIds();
      // assertEquals(15, cids.size());
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  @Category(Slow.class)
  public void testInstrument() {
    try {
      VulasConfiguration.getGlobal().setProperty(CoreConfiguration.INSTR_WRITE_CODE, "true");
      SpringBootAnalyzer.setAppContext(
          new Application("dummy-group", "dummy-artifact", "0.0.1-SNAPSHOT"));
      final SpringBootAnalyzer wa = new SpringBootAnalyzer();
      wa.analyze(new File("./src/test/resources/boot-app.jar"));
      wa.setWorkDir(Paths.get("./target"));
      wa.setRename(true);
      wa.setInstrument(true);
      wa.call();

      // Check instrumented WAR
      final File new_war = wa.getInstrumentedArchive();
      assertTrue(new_war.exists());
      System.out.println(
          "Size of rewritten archive [" + new_war.getName() + "]: " + new_war.length());
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
}
