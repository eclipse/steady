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
package com.sap.psr.vulas.backend.requests;

import java.io.IOException;
import java.io.Serializable;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.HttpResponse;
import com.sap.psr.vulas.goals.GoalContext;

/**
 * Http request that can be send and saved to (loaded from) disk.
 */
public interface HttpRequest extends Serializable {

  /**
   * <p>send.</p>
   *
   * @return a {@link com.sap.psr.vulas.backend.HttpResponse} object.
   * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
   */
  public HttpResponse send() throws BackendConnectionException;

  /**
   * <p>getGoalContext.</p>
   *
   * @return a {@link com.sap.psr.vulas.goals.GoalContext} object.
   */
  public GoalContext getGoalContext();

  /**
   * <p>setGoalContext.</p>
   *
   * @param _ctx a {@link com.sap.psr.vulas.goals.GoalContext} object.
   * @return a {@link com.sap.psr.vulas.backend.requests.HttpRequest} object.
   */
  public HttpRequest setGoalContext(GoalContext _ctx);

  /**
   * <p>saveToDisk.</p>
   *
   * @throws java.io.IOException if any.
   */
  public void saveToDisk() throws IOException;

  /**
   * <p>savePayloadToDisk.</p>
   *
   * @throws java.io.IOException if any.
   */
  public void savePayloadToDisk() throws IOException;

  /**
   * <p>loadFromDisk.</p>
   *
   * @throws java.io.IOException if any.
   */
  public void loadFromDisk() throws IOException;

  /**
   * <p>loadPayloadFromDisk.</p>
   *
   * @throws java.io.IOException if any.
   */
  public void loadPayloadFromDisk() throws IOException;

  /**
   * <p>deleteFromDisk.</p>
   *
   * @throws java.io.IOException if any.
   */
  public void deleteFromDisk() throws IOException;

  /**
   * <p>deletePayloadFromDisk.</p>
   *
   * @throws java.io.IOException if any.
   */
  public void deletePayloadFromDisk() throws IOException;

  /**
   * <p>getFilename.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFilename();
}
