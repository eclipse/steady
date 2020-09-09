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
package org.eclipse.steady.shared.enums;

/**
 * Defines the maturity of database content. Can be used, for instance, to configure a space in regards
 * to bugs that shall be considered for application analysis.
 */
public enum ContentMaturityLevel {
  DRAFT((byte) 1),
  READY((byte) 2);
  private byte value;

  private ContentMaturityLevel(byte _value) {
    this.value = _value;
  }
  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    if (this.value == 1) return "DRAFT";
    else if (this.value == 2) return "READY";
    else throw new IllegalArgumentException("[" + this.value + "] is not a valid maturity level");
  }
}
