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
package org.eclipse.steady.shared.enums;

import java.security.MessageDigest;

import org.eclipse.steady.shared.util.FileUtil;

/**
 * Enumeration of different digest algorithms, to be used when computing the digest of libraries with {@link FileUtil#getDigest(java.io.File, DigestAlgorithm)}.
 *
 * @see MessageDigest
 */
public enum DigestAlgorithm {
  SHA1((byte) 10),
  SHA256((byte) 20),
  MD5((byte) 30);

  private byte value;

  private DigestAlgorithm(byte _value) {
    this.value = _value;
  }

  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    if (this.value == 10) return "SHA1";
    else if (this.value == 20) return "SHA256";
    else if (this.value == 30) return "MD5";
    else throw new IllegalArgumentException("[" + this.value + "] is not a valid digest algorithm");
  }
}
