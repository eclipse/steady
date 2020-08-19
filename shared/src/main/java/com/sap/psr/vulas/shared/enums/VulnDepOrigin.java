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
package com.sap.psr.vulas.shared.enums;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;

/**
 * Use in com.sap.psr.vulas.backend.model.VulnerableDependency and com.sap.psr.vulas.shared.json.model.VulnerableDependency
 */
public enum VulnDepOrigin {
  CC,
  BUNDLEDCC,
  AFFLIBID,
  BUNDLEDAFFLIBID;

  /** Constant <code>log</code> */
  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * <p>fromStringArray.</p>
   *
   * @param _values an array of {@link java.lang.String} objects.
   * @return a {@link java.util.Set} object.
   */
  public static final Set<VulnDepOrigin> fromStringArray(String[] _values) {
    final Set<VulnDepOrigin> vulndeporigins = new HashSet<VulnDepOrigin>();
    if (_values != null) {
      for (String v : _values) {
        try {
          vulndeporigins.add(VulnDepOrigin.valueOf(v.toUpperCase()));
        } catch (IllegalArgumentException e) {
          VulnDepOrigin.log.warn(
              "Invalid vulndeporigin ["
                  + v
                  + "] ignored, use one of [CC, BUNDLEDCC, AFFLIBID, BUNDLEDAFFLIBID]");
        }
      }
    }
    return vulndeporigins;
  }

  /**
   * <p>fromString.</p>
   *
   * @param _value a {@link java.lang.String} object.
   * @param _default a {@link com.sap.psr.vulas.shared.enums.VulnDepOrigin} object.
   * @return a {@link com.sap.psr.vulas.shared.enums.VulnDepOrigin} object.
   */
  public static VulnDepOrigin fromString(String _value, VulnDepOrigin _default) {
    try {
      return VulnDepOrigin.valueOf(_value.toUpperCase());
    } catch (IllegalArgumentException iae) {
      VulnDepOrigin.log.warn(
          "Invalid vulndeporigin [" + _value + "], returning default [" + _default + "]");
      return _default;
    }
  }
}
