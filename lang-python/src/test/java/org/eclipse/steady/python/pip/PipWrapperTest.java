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
package org.eclipse.steady.python.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.python.ProcessWrapperException;
import org.eclipse.steady.python.pip.PipWrapper.PipPackageJson;
import org.eclipse.steady.shared.categories.Slow;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.ThreadUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PipWrapperTest {

  @Test
  public void testIsPipAvailable() throws ProcessWrapperException {
    final PipWrapper pip = new PipWrapper();
    final boolean is_available = pip.isAvailable();
  }

  /**
   * Reads all installed PIP packages from the Python environment.
   * @throws ProcessWrapperException
   */
  @Test
  public void testGetListPackages() throws ProcessWrapperException, FileAnalysisException {
    VulasConfiguration.getGlobal().setProperty(ThreadUtil.NO_OF_THREADS, "AUTO");
    final PipWrapper pip = new PipWrapper();
    Set<PipInstalledPackage> packs = null;
    try {
      packs = pip.getListPackages();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertTrue(packs.size() > 0);
  }

  /**
   * Reads all installed PIP packages from the Python environment.
   * @throws ProcessWrapperException
   */
  @Test
  @Category(Slow.class)
  public void testGetFreezePackages() throws ProcessWrapperException, FileAnalysisException {
    VulasConfiguration.getGlobal().setProperty(ThreadUtil.NO_OF_THREADS, "AUTO");
    final PipWrapper pip = new PipWrapper();
    Set<PipInstalledPackage> packs = null;
    try {
      packs = pip.getFreezePackages();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertTrue(packs.size() > 0);

    // Compute the digests
    for (PipInstalledPackage p : packs) {
      System.out.println(p);
      /*if(p.getName().equals("setuptools")) {
      	final String sha1 = p.getSha1();
      	assertTrue(sha1!=null && sha1.length()>0);
      	assertTrue(p.getConstructs().size()>0);
      }*/
    }
  }

  @Test
  public void testPipDownloadUrl() {
    final String str =
        "  Downloading"
            + " http://foo.bar.com:1234/nexus/content/groups/build.snapshots.pypi/packages/77/32/e3597cb19ffffe724ad4bf0beca4153419918e7fa4ba6a34b04ee4da3371/Flask-0.12.2-py2.py3-none-any.whl"
            + " (83kB)";
    final String url =
        "http://foo.bar.com:1234/nexus/content/groups/build.snapshots.pypi/packages/77/32/e3597cb19ffffe724ad4bf0beca4153419918e7fa4ba6a34b04ee4da3371/Flask-0.12.2-py2.py3-none-any.whl";
    final Matcher m = PipWrapper.DOWNLOAD_PATTERN.matcher(str);
    assertTrue(m.matches());
    final String g1 = m.group(1);
    assertEquals(url, g1);
  }

  @Test
  public void testParsePipInstallOutput() throws IOException, ProcessWrapperException {
    final String out = FileUtil.readFile("./src/test/resources/pip-install-out.txt");
    final Set<PipInstalledPackage> packs = new HashSet<PipInstalledPackage>();

    final PipInstalledPackage werkzeug = new PipInstalledPackage("Werkzeug", "0.12.2");
    packs.add(werkzeug);

    final PipInstalledPackage ms = new PipInstalledPackage("MarkupSafe", "1.0");
    packs.add(ms);

    final PipInstalledPackage isdang = new PipInstalledPackage("itsdangerous", "0.24");
    packs.add(isdang);

    final PipWrapper pw = new PipWrapper();
    pw.searchDownloadInfo(packs, out);

    assertTrue(werkzeug.getDownloadUrl() != null);
    assertTrue(ms.getDownloadUrl() != null);
    assertTrue(isdang.getDownloadUrl() != null);
  }

  @Test
  public void testParsePipListJson() throws IOException, ProcessWrapperException {
    String json = FileUtil.readFile("./src/test/resources/pip-list-old.json");
    PipPackageJson[] packs = (PipPackageJson[]) JacksonUtil.asObject(json, PipPackageJson[].class);
    assertEquals(2, packs.length);

    json = FileUtil.readFile("./src/test/resources/pip-list-new.json");
    packs = (PipPackageJson[]) JacksonUtil.asObject(json, PipPackageJson[].class);
    assertEquals(2, packs.length);
  }
}
