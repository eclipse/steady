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
package org.eclipse.steady.shared.json.model.mavenCentral;

import java.util.Collection;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corresponds to the JSON object structure returned by the RESTful search of the Maven Central.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenSearchResponse {

  private long numFound;

  private long start;

  private Collection<ResponseDoc> docs;

  /**
   * <p>Constructor for MavenSearchResponse.</p>
   */
  public MavenSearchResponse() {}

  /**
   * <p>Getter for the field <code>numFound</code>.</p>
   *
   * @return a long.
   */
  public long getNumFound() {
    return numFound;
  }
  /**
   * <p>Setter for the field <code>numFound</code>.</p>
   *
   * @param numFound a long.
   */
  public void setNumFound(long numFound) {
    this.numFound = numFound;
  }

  /**
   * <p>Getter for the field <code>start</code>.</p>
   *
   * @return a long.
   */
  public long getStart() {
    return start;
  }
  /**
   * <p>Setter for the field <code>start</code>.</p>
   *
   * @param start a long.
   */
  public void setStart(long start) {
    this.start = start;
  }

  /**
   * <p>Getter for the field <code>docs</code>.</p>
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<ResponseDoc> getDocs() {
    return docs;
  }
  /**
   * <p>Setter for the field <code>docs</code>.</p>
   *
   * @param docs a {@link java.util.Collection} object.
   */
  public void setDocs(Collection<ResponseDoc> docs) {
    this.docs = docs;
  }

  /**
   * <p>getSortedDocs.</p>
   *
   * @return a {@link java.util.TreeSet} object.
   */
  @JsonIgnore
  public TreeSet<ResponseDoc> getSortedDocs() {
    final TreeSet<ResponseDoc> set = new TreeSet<ResponseDoc>();
    set.addAll(this.getDocs());
    return set;
  }
}
