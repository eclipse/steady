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
package org.eclipse.steady.backend.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.steady.shared.util.FileUtil;
import org.junit.Test;

public class PyPiVerifierTest {

  @Test
  public void testVerify() throws IOException {
    final String json = FileUtil.readFile("./src/test/resources/pypi_flask.json");

    PyPiVerifier ppv = new PyPiVerifier();
    assertTrue(ppv.containsMD5(json, "c1d30f51cff4a38f9454b23328a15c5a")); // Flask 0.11
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    c.setTimeInMillis(1482351732000L);
    assertEquals(c, ppv.getReleaseTimestamp());

    ppv = new PyPiVerifier();
    assertFalse(ppv.containsMD5(json, "c1d30f51cff4a38f9454b23328a15c5azzzzz")); // Does not exist
    assertEquals(null, ppv.getReleaseTimestamp());
  }
}
