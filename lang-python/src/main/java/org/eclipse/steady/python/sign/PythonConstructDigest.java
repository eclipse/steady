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
package org.eclipse.steady.python.sign;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.eclipse.steady.shared.json.JsonBuilder;
import org.eclipse.steady.shared.util.DigestUtil;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.sign.Signature;

/**
 * <p>PythonConstructDigest class.</p>
 *
 */
public class PythonConstructDigest implements Signature {

  public static enum ComputedFromType {
    FILE,
    BODY
  }

  private static final int MAX_STRING_LENGTH = 100;

  private String computedFrom = null;

  private ComputedFromType computedFromType = null;

  private String digest = null;

  private DigestAlgorithm digestAlgorithm = null;

  /**
   * <p>Constructor for PythonConstructDigest.</p>
   *
   * @param _path a {@link java.nio.file.Path} object.
   * @param _alg a {@link org.eclipse.steady.shared.enums.DigestAlgorithm} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public PythonConstructDigest(Path _path, DigestAlgorithm _alg) throws IllegalArgumentException {
    if (!FileUtil.isAccessibleFile(_path))
      throw new IllegalArgumentException("Path argument [" + _path + "] is not a valid file");
    this.digest = FileUtil.getDigest(_path.toFile(), _alg);
    this.digestAlgorithm = _alg;
    this.computedFrom = _path.getFileName().toString();
    this.computedFromType = ComputedFromType.FILE;
  }

  /**
   * <p>Constructor for PythonConstructDigest.</p>
   *
   * @param _string a {@link java.lang.String} object.
   * @param _alg a {@link org.eclipse.steady.shared.enums.DigestAlgorithm} object.
   */
  public PythonConstructDigest(String _string, DigestAlgorithm _alg) {
    if (_string == null) throw new IllegalArgumentException("String argument cannot be null");
    this.digest = DigestUtil.getDigestAsString(_string, StandardCharsets.UTF_8, _alg);
    this.digestAlgorithm = _alg;
    if (_string.length() > MAX_STRING_LENGTH)
      this.computedFrom = _string.substring(0, MAX_STRING_LENGTH - 3) + "...";
    else this.computedFrom = _string;
    this.computedFromType = ComputedFromType.BODY;
  }

  /**
   * <p>Getter for the field <code>computedFrom</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getComputedFrom() {
    return computedFrom;
  }

  /**
   * <p>Setter for the field <code>computedFrom</code>.</p>
   *
   * @param computedFrom a {@link java.lang.String} object.
   */
  public void setComputedFrom(String computedFrom) {
    this.computedFrom = computedFrom;
  }

  /**
   * <p>Getter for the field <code>computedFromType</code>.</p>
   *
   * @return a {@link org.eclipse.steady.python.sign.PythonConstructDigest.ComputedFromType} object.
   */
  public ComputedFromType getComputedFromType() {
    return computedFromType;
  }

  /**
   * <p>Setter for the field <code>computedFromType</code>.</p>
   *
   * @param computedFromType a {@link org.eclipse.steady.python.sign.PythonConstructDigest.ComputedFromType} object.
   */
  public void setComputedFromType(ComputedFromType computedFromType) {
    this.computedFromType = computedFromType;
  }

  /**
   * <p>Getter for the field <code>digest</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDigest() {
    return digest;
  }

  /**
   * <p>Setter for the field <code>digest</code>.</p>
   *
   * @param digest a {@link java.lang.String} object.
   */
  public void setDigest(String digest) {
    this.digest = digest;
  }

  /**
   * <p>Getter for the field <code>digestAlgorithm</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.enums.DigestAlgorithm} object.
   */
  public DigestAlgorithm getDigestAlgorithm() {
    return digestAlgorithm;
  }

  /**
   * <p>Setter for the field <code>digestAlgorithm</code>.</p>
   *
   * @param digestAlgorithm a {@link org.eclipse.steady.shared.enums.DigestAlgorithm} object.
   */
  public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) {
    this.digestAlgorithm = digestAlgorithm;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return this.digest + " (" + this.digestAlgorithm + ")";
  }

  /** {@inheritDoc} */
  @Override
  public String toJson() {
    final JsonBuilder b = new JsonBuilder();
    b.startObject();
    b.appendObjectProperty("digest", this.digest);
    b.appendObjectProperty("digestAlgorithm", this.digestAlgorithm.toString());
    b.appendObjectProperty("computedFrom", this.computedFrom);
    b.appendObjectProperty("computedFromType", this.computedFromType.toString());
    b.endObject();
    return b.toString();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((digest == null) ? 0 : digest.hashCode());
    result = prime * result + ((digestAlgorithm == null) ? 0 : digestAlgorithm.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PythonConstructDigest other = (PythonConstructDigest) obj;
    if (digest == null) {
      if (other.digest != null) return false;
    } else if (!digest.equals(other.digest)) return false;
    if (digestAlgorithm != other.digestAlgorithm) return false;
    return true;
  }
}
