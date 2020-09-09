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
package org.eclipse.steady.shared.json.model.mavenCentral;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corresponds to the JSON object structure returned by the RESTful search of the Maven Central.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenVersionsSearch {

  private MavenSearchResponse response;

  /**
   * <p>Constructor for MavenVersionsSearch.</p>
   */
  public MavenVersionsSearch() {}

  /**
   * <p>Getter for the field <code>response</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.mavenCentral.MavenSearchResponse} object.
   */
  public MavenSearchResponse getResponse() {
    return response;
  }
  /**
   * <p>Setter for the field <code>response</code>.</p>
   *
   * @param response a {@link org.eclipse.steady.shared.json.model.mavenCentral.MavenSearchResponse} object.
   */
  public void setResponse(MavenSearchResponse response) {
    this.response = response;
  }
}
