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

import com.sap.psr.vulas.shared.json.model.ConstructId;

/**
 * Defines the type of modification of a given {@link ConstructId}.
 *
 * Change types {@link ConstructChangeType#ADD}, {@link ConstructChangeType#MOD} and {@link ConstructChangeType#DEL}
 * apply both in the context of a single commit and multiple commits, whereas {@link ConstructChangeType#NUL} is only
 * meaningful in the context of multiple commits.
 *
 * When used in the context of multiple commits, the {@link ConstructChangeType} indicates the overall modification done over the
 * course of the ordered commits.
 *
 * {@link ConstructChangeType#ADD} means that the construct has been added,
 * {@link ConstructChangeType#MOD} means it has been modified,
 * {@link ConstructChangeType#DEL} means it has been deleted, and
 * {@link ConstructChangeType#NUL} means that the overall change is null, which happens when
 * the construct has been deleted in an early commit and added in a later commit (or vice-versa).
 */
public enum ConstructChangeType {
  ADD((byte) 10),
  MOD((byte) 20),
  DEL((byte) 30),
  NUL((byte) 40);
  private byte value;

  private ConstructChangeType(byte _value) {
    this.value = _value;
  }
  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    if (this.value == 10) return "ADD";
    else if (this.value == 20) return "MOD";
    else if (this.value == 30) return "DEL";
    else if (this.value == 40) return "NUL";
    else
      throw new IllegalArgumentException(
          "[" + this.value + "] is not a valid construct change type");
  }
}
