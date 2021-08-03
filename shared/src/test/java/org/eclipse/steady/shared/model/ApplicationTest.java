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
package org.eclipse.steady.shared.model;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.util.Constants;
import org.eclipse.steady.shared.util.FileUtil;
import org.junit.Test;

public class ApplicationTest {

  @Test(expected = IllegalArgumentException.class)
  public void testArgumentNull() {
    new Application(null, "foo", "bar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArgumentTooLong() {
    final StringBuilder g = new StringBuilder();
    for (int i = 0; i <= Constants.MAX_LENGTH_GROUP + 10; i++) g.append("a");
    new Application(g.toString(), "foo", "bar");
  }

  @Test
  public void testAppReachDeserialization() {
    try {
      String json = FileUtil.readFile("./src/test/resources/appReachConstructIds.json");
      org.eclipse.steady.shared.json.model.Dependency[] backend_deps = (org.eclipse.steady.shared.json.model.Dependency[]) JacksonUtil
        .asObject(json, org.eclipse.steady.shared.json.model.Dependency[].class);
      assertNotNull(backend_deps[0].getReachableConstructIds());
    } catch (IOException e) {
      assert(false);
    }
    
  }
}
