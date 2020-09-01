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
package org.eclipse.steady.backend.requests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.steady.backend.HttpResponse;

/**
 * <p>ContentCondition class.</p>
 *
 */
public class ContentCondition implements ResponseCondition {

  public enum Mode {
    MATCH,
    EQ_STRING,
    LT_DOUBLE,
    GT_DOUBLE
  }

  private String regex = null;

  private Mode mode = Mode.MATCH;

  private String value = null;

  private Pattern pattern = null; // Pattern.compile("\\\"constructCounter\\\"\\s*:\\s*[\\d]*");

  /**
   * <p>Constructor for ContentCondition.</p>
   *
   * @param _regex a {@link java.lang.String} object.
   * @param _mode a {@link org.eclipse.steady.backend.requests.ContentCondition.Mode} object.
   * @param _value a {@link java.lang.String} object.
   */
  public ContentCondition(String _regex, Mode _mode, String _value) {
    this.regex = _regex;
    this.pattern = Pattern.compile(this.regex);
    this.mode = _mode;
    this.value = _value;
  }

  /**
   * {@inheritDoc}
   *
   * Returns true if the content of the given {@link HttpResponse} matches the regular expression of the condition, false otherwise.
   */
  @Override
  public boolean meetsCondition(HttpResponse _response) {
    if (_response == null || !_response.hasBody()) return false;

    boolean meets = false;
    final Matcher m = pattern.matcher(_response.getBody());
    if (this.mode.equals(Mode.MATCH)) {
      meets = m.matches();
    } else if (this.mode.equals(Mode.EQ_STRING)) {
      meets = m.find() && this.value.equals(m.group(1));
    } else if (this.mode.equals(Mode.LT_DOUBLE)) {
      if (m.find()) {
        double actual = Double.parseDouble(m.group(1));
        meets = actual < Double.parseDouble(this.value);
      }
    } else if (this.mode.equals(Mode.GT_DOUBLE)) {
      if (m.find()) {
        double actual = Double.parseDouble(m.group(1));
        meets = actual > Double.parseDouble(this.value);
      }
    }

    return meets;
  }

  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return "[body " + this.mode + " " + this.value + "]";
  }
}
