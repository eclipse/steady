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
package org.eclipse.steady.cia.dependencyfinder;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.steady.cia.dependencyfinder.JarDiffCmd;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.model.Artifact;
import org.eclipse.steady.shared.json.model.diff.JarDiffResult;
import org.junit.Test;

public class JarDiffCmdTest {

  @Test
  public void testDoProcessing() {
    Artifact old_doc = new Artifact("commons-fileupload", "commons-fileupload", "1.1.1");

    Artifact new_doc = new Artifact("commons-fileupload", "commons-fileupload", "1.3.1");

    Path old_jar = Paths.get("./src/test/resources/commons-fileupload-1.1.1.jar");
    Path new_jar = Paths.get("./src/test/resources/commons-fileupload-1.3.1.jar");

    JarDiffCmd cmd = new JarDiffCmd(old_doc, old_jar, new_doc, new_jar);

    try {
      final String[] args =
          new String[] {
            "-old", old_jar.toString(), "-new", new_jar.toString(), "-name", "xyz", "-code"
          };
      cmd.run(args);
    } catch (Exception e) {
      e.printStackTrace();
    }

    JarDiffResult result = cmd.getResult();

    String json = JacksonUtil.asJsonString(result);
    System.out.println(json);
    assertTrue(result != null);
  }
}
