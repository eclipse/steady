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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.steady.shared.util.StringUtil;
import org.junit.Test;

public class StringUtilTest {

  @Test
  public void testJoin() {
    final String[] a = new String[] {"a", "b"};
    final String j1 = StringUtil.join(a, ", ");
    assertEquals("a, b", j1);

    final List<String> s = new ArrayList<String>();
    s.add("1");
    s.add("2");
    final String j2 = StringUtil.join(s, ", ");
    assertEquals("1, 2", j2);
  }

  @Test
  public void testIsEmptyOrContainsEmptyString() {
    assertEquals(true, StringUtil.isEmptyOrContainsEmptyString(null));
    assertEquals(true, StringUtil.isEmptyOrContainsEmptyString(new String[] {}));
    assertEquals(true, StringUtil.isEmptyOrContainsEmptyString(new String[] {""}));
    assertEquals(false, StringUtil.isEmptyOrContainsEmptyString(new String[] {"foo"}));
    assertEquals(false, StringUtil.isEmptyOrContainsEmptyString(new String[] {"foo", "bar"}));
  }

  @Test
  public void testPadLeft() {
    assertEquals("  1", StringUtil.padLeft(1, 3)); // Padding of a shorter string representation
    assertEquals(
        "11111",
        StringUtil.padLeft(11111, 3)); // Padding of a longer string representation must not cut
  }
}
