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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.junit.Test;

public class DigestUtilTest {

  @Test
  public void testGetDigestAsBytes() throws IOException {
    final String text = "foo bar baz";
    final String sha1 =
        DigestUtil.getDigestAsString(text, StandardCharsets.UTF_8, DigestAlgorithm.SHA1)
            .toLowerCase();
    final String expected_sha1 = "c7567e8b39e2428e38bf9c9226ac68de4c67dc39";
    assertEquals(expected_sha1, sha1);
  }

  @Test
  public void testGetDigestAsBytesFromFile() throws IOException {
    final String text = FileUtil.readFile("./src/test/resources/foo.txt");
    assertEquals("foo bar baz", text);
    final String sha1 =
        DigestUtil.getDigestAsString(text, StandardCharsets.UTF_8, DigestAlgorithm.SHA1)
            .toLowerCase();
    final String expected_sha1 = "c7567e8b39e2428e38bf9c9226ac68de4c67dc39";
    assertEquals(expected_sha1, sha1);
  }
}
