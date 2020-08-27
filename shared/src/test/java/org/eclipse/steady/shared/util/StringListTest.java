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

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.steady.shared.util.StringList;
import org.eclipse.steady.shared.util.StringList.CaseSensitivity;
import org.eclipse.steady.shared.util.StringList.ComparisonMode;
import org.junit.Test;

public class StringListTest {

  @Test
  public void testContains() {
    final String[] itemlist = new String[] {"foo.bar"};

    // Use as blacklist for classnames
    final StringList blacklist = new StringList(itemlist);
    assertTrue(blacklist.contains("foo.bar"));
    assertTrue(!blacklist.contains("FOO.bar"));
    assertTrue(
        blacklist.contains("FOO.bar", ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE));
    assertTrue(!blacklist.contains("top.down"));

    // Use as whitelist for packages
    final StringList whitelist = new StringList(itemlist);
    assertTrue(
        whitelist.contains(
            "foo.bar.Test", ComparisonMode.STARTSWITH, CaseSensitivity.CASE_SENSITIVE));
    assertTrue(
        whitelist.contains(
            "foo.BAR.Test", ComparisonMode.STARTSWITH, CaseSensitivity.CASE_INSENSITIVE));
    assertTrue(
        !whitelist.contains(
            "foo.BAR.Test", ComparisonMode.STARTSWITH, CaseSensitivity.CASE_SENSITIVE));
    assertTrue(
        !whitelist.contains(
            "top.down.Test", ComparisonMode.STARTSWITH, CaseSensitivity.CASE_SENSITIVE));

    // Use as blacklist for JAR file names
    final StringList jar_bl = new StringList(new String[] {"vulas-core-.*\\.jar"});
    assertTrue(
        jar_bl.contains(
            "vulas-core-1.1.0-SNAPSHOT-jar-with-dependencies.jar",
            ComparisonMode.PATTERN,
            CaseSensitivity.CASE_INSENSITIVE));
    assertTrue(
        !jar_bl.contains(
            "commons-fileupload-1.2.1.jar",
            ComparisonMode.PATTERN,
            CaseSensitivity.CASE_INSENSITIVE));
  }

  @Test
  public void testFilter() {
    final StringList list = new StringList(new String[] {"foo.BAR"});

    final Map<String, String> m = new HashMap<String, String>();
    m.put("foo.bar", "abc");
    m.put("john.doe", "123");

    // keep_matches = true
    final Map<String, String> o1 =
        list.filter(m, true, ComparisonMode.EQUALS, CaseSensitivity.CASE_SENSITIVE);
    assertTrue(!o1.containsKey("foo.bar"));
    assertTrue(!o1.containsKey("john.doe"));

    final Map<String, String> o3 =
        list.filter(m, true, ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE);
    assertTrue(o3.containsKey("foo.bar"));
    assertTrue(!o3.containsKey("john.doe"));

    // keep_matches = false
    final Map<String, String> o2 =
        list.filter(m, false, ComparisonMode.EQUALS, CaseSensitivity.CASE_SENSITIVE);
    assertTrue(o2.containsKey("foo.bar"));
    assertTrue(o2.containsKey("john.doe"));

    final Map<String, String> o4 =
        list.filter(m, false, ComparisonMode.EQUALS, CaseSensitivity.CASE_INSENSITIVE);
    assertTrue(!o4.containsKey("foo.bar"));
    assertTrue(o4.containsKey("john.doe"));
  }
}
