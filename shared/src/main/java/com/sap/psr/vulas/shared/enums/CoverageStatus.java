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

/**
 * <p>CoverageStatus class.</p>
 *
 */
public enum CoverageStatus {
  COVERED((byte) 10),
  OPEN((byte) 20),
  OUT_OF_SCOPE((byte) 30),
  UNKNOWN((byte) 40),
  ERROR((byte) 50);

  private byte value;

  private CoverageStatus(byte _value) {
    this.value = _value;
  }

  /**
   * <p>getStatusCode.</p>
   *
   * @return a byte.
   */
  public byte getStatusCode() {
    return this.value;
  }

  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    if (this.value == 10) return "Covered";
    else if (this.value == 20) return "Open";
    else if (this.value == 30) return "Out-of-scope";
    else if (this.value == 40) return "Unknown";
    else if (this.value == 50) return "Error";
    else throw new IllegalArgumentException("[" + this.value + "] is not a valid coverage status");
  }

  /**
   * <p>getText.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getText() {
    return this.toString();
  }

  /**
   * <p>getDescription.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDescription() {
    if (this.value == 10) return "Vulnerability part of Vulas database";
    else if (this.value == 20) return "Vulnerability under investigation";
    else if (this.value == 30) return "Vulnerability out of scope for Vulas";
    else if (this.value == 40) return "Vulnerability not known to Vulas";
    else if (this.value == 50) return "Error when establishing coverage";
    else throw new IllegalArgumentException("[" + this.value + "] is not a valid coverage status");
  }
}
