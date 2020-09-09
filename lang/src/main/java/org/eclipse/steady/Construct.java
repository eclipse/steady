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
package org.eclipse.steady;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.enums.DigestAlgorithm;
import org.eclipse.steady.shared.json.JsonBuilder;
import org.eclipse.steady.shared.util.DigestUtil;

/**
 * A programming construct represents a container for programming statements in a given programming language, e.g., a Java constructor or a C function.
 * Programming constructs are created by instances of FileAnalyzer, e.g., JavaFileAnalyzer or JavaClassAnalyzer.
 */
public class Construct {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();
  private ConstructId id = null;
  private String content, contentDigest = null;

  /**
   * <p>Constructor for Construct.</p>
   *
   * @param _id a {@link org.eclipse.steady.ConstructId} object.
   * @param _content a {@link java.lang.String} object.
   */
  public Construct(ConstructId _id, String _content) {
    if (_id == null || _content == null)
      throw new IllegalArgumentException("Id and content must be provided");
    this.id = _id;
    this.setContent(_content);
  }
  /**
   * <p>getDigest.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getDigest() {
    return contentDigest;
  }
  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a {@link org.eclipse.steady.ConstructId} object.
   */
  public ConstructId getId() {
    return id;
  }
  /**
   * <p>Getter for the field <code>content</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getContent() {
    return content;
  }
  /**
   * <p>Setter for the field <code>content</code>.</p>
   *
   * @param _content a {@link java.lang.String} object.
   */
  public void setContent(String _content) {
    this.content = _content;
    this.contentDigest =
        DigestUtil.getDigestAsString(_content, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
  }
  /**
   * <p>toJSON.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toJSON() {
    final JsonBuilder jb = new JsonBuilder();
    jb.startObject();
    jb.appendObjectProperty("id", this.id.toJSON(), false);
    jb.appendObjectProperty("cd", this.contentDigest);
    jb.endObject();
    return jb.getJson();
  }
  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contentDigest == null) ? 0 : contentDigest.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }
  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Construct other = (Construct) obj;
    if (contentDigest == null) {
      if (other.contentDigest != null) return false;
    } else if (!contentDigest.equals(other.contentDigest)) return false;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    return true;
  }
  /**
   * <p>toString.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return this.id.toString();
  }
}
