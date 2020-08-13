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
package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Space;

/**
 * <p>SpaceRepositoryCustom interface.</p>
 *
 */
public interface SpaceRepositoryCustom {

  /**
   * <p>customSave.</p>
   *
   * @param _lib a {@link com.sap.psr.vulas.backend.model.Space} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Space} object.
   */
  public Space customSave(Space _lib);

  /**
   * Returns the default space for the given tenant token (default tenant used if null).
   *
   * @param _tenantToken a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Space} object.
   */
  public Space getDefaultSpace(String _tenantToken);

  /**
   * Returns the space for the given space token (default space of default tenant used if null).
   *
   * @param _spaceToken a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.model.Space} object.
   */
  public Space getSpace(String _spaceToken);
}
