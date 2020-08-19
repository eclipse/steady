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
package com.sap.psr.vulas.shared.cache;

/**
 * <p>CacheException class.</p>
 *
 */
public class CacheException extends Exception {

  /**
   * <p>Constructor for CacheException.</p>
   *
   * @param _key a {@link java.lang.Object} object.
   */
  public CacheException(Object _key) {
    super("Cache problem with key [" + _key.toString() + "]");
  }

  /**
   * <p>Constructor for CacheException.</p>
   *
   * @param _key a {@link java.lang.Object} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public CacheException(Object _key, Throwable _cause) {
    super("Cache problem with key [" + _key.toString() + "]", _cause);
  }
}
