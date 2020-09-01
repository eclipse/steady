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
package org.eclipse.steady.cia.rest;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.steady.cia.util.MavenCentralWrapper;
import org.eclipse.steady.shared.json.model.Artifact;
import org.junit.Test;

public class IT03_ClassControllerTest {

  @Test
  public void getArtifactsForClassTest() {

    MavenCentralWrapper r = new MavenCentralWrapper();
    try {
      Set<Artifact> response =
          r.getArtifactForClass(
              "org.apache.commons.fileupload.MultipartStream", "1000", null, "jar");

      System.out.println(response.size());
      assertTrue(response.size() >= 172);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
