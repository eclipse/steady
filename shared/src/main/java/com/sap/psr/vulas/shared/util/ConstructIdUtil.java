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
package com.sap.psr.vulas.shared.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.shared.json.model.ConstructId;

/**
 * <p>ConstructIdUtil class.</p>
 *
 */
public class ConstructIdUtil {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * <p>filterWithRegex.</p>
   *
   * @param _in a {@link java.util.Set} object.
   * @param _qname_filter an array of {@link java.lang.String} objects.
   * @return a {@link java.util.Set} object.
   */
  public static Set<ConstructId> filterWithRegex(Set<ConstructId> _in, String[] _qname_filter) {
    final Set<ConstructId> result = new HashSet<ConstructId>();
    final Set<Pattern> filter = new HashSet<Pattern>();
    Matcher m = null;
    for (String f : _qname_filter) filter.add(Pattern.compile(f));
    final int count_before = _in.size();
    for (ConstructId c : _in) {
      for (Pattern p : filter) {
        m = p.matcher(c.getQname());
        if (m.matches()) result.add(c);
      }
    }
    final int count_after = result.size();
    log.info("[" + (count_before - count_after) + "/" + count_before + "] items filtered");
    return result;
  }
}
